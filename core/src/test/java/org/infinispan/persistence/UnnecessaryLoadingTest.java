package org.infinispan.persistence;

import static org.testng.Assert.assertEquals;

import java.util.concurrent.Executor;
import java.util.function.Predicate;

import org.infinispan.Cache;
import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.commons.configuration.ConfigurationFor;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.commons.util.IntSet;
import org.infinispan.configuration.cache.AbstractStoreConfiguration;
import org.infinispan.configuration.cache.AbstractStoreConfigurationBuilder;
import org.infinispan.configuration.cache.AsyncStoreConfiguration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.context.InvocationContext;
import org.infinispan.context.InvocationContextFactory;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.marshall.TestObjectStreamMarshaller;
import org.infinispan.marshall.persistence.impl.MarshalledEntryUtil;
import org.infinispan.persistence.dummy.DummyInMemoryStore;
import org.infinispan.persistence.dummy.DummyInMemoryStoreConfigurationBuilder;
import org.infinispan.persistence.dummy.Element;
import org.infinispan.persistence.manager.PersistenceManager;
import org.infinispan.persistence.manager.PersistenceManagerImpl;
import org.infinispan.persistence.spi.InitializationContext;
import org.infinispan.persistence.spi.MarshallableEntry;
import org.infinispan.persistence.spi.PersistenceException;
import org.infinispan.persistence.spi.SegmentedAdvancedLoadWriteStore;
import org.infinispan.test.SingleCacheManagerTest;
import org.infinispan.test.TestingUtil;
import org.infinispan.test.fwk.CleanupAfterMethod;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.infinispan.util.concurrent.CompletionStages;
import org.infinispan.util.concurrent.IsolationLevel;
import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

/**
 * A test to ensure stuff from a cache store is not loaded unnecessarily if it already exists in memory, or if the
 * Flag.SKIP_CACHE_LOAD is applied.
 *
 * @author Manik Surtani
 * @author Sanne Grinovero
 * @version 4.1
 */
@Test(testName = "persistence.UnnecessaryLoadingTest", groups = "functional", singleThreaded = true)
@CleanupAfterMethod
public class UnnecessaryLoadingTest extends SingleCacheManagerTest {
   DummyInMemoryStore store;
   private PersistenceManagerImpl persistenceManager;

   @Override
   protected EmbeddedCacheManager createCacheManager() throws Exception {
      ConfigurationBuilder cfg = getDefaultStandaloneCacheConfig(true);
      cfg
         .invocationBatching().enable()
         .persistence()
            .addStore(CountingStoreConfigurationBuilder.class)
         .persistence()
            .addStore(DummyInMemoryStoreConfigurationBuilder.class)
         .locking().isolationLevel(IsolationLevel.READ_COMMITTED); //avoid versioning since we are storing directly in CacheStore
      return TestCacheManagerFactory.createCacheManager(cfg);
   }

   @Override
   protected void setup() throws Exception {
      super.setup();
      persistenceManager = (PersistenceManagerImpl) TestingUtil.extractComponent(cache, PersistenceManager.class);
      store = TestingUtil.getStore(cache, 1, false);

   }

   public void testRepeatedLoads() throws PersistenceException {
      CountingStore countingCS = getCountingCacheStore();
      store.write(MarshalledEntryUtil.create("k1", "v1", cache));

      assert countingCS.numLoads == 0;
      assert countingCS.numContains == 0;

      assert "v1".equals(cache.get("k1"));

      assert countingCS.numLoads == 1 : "Expected 1, was " + countingCS.numLoads;
      assert countingCS.numContains == 0 : "Expected 0, was " + countingCS.numContains;

      assert "v1".equals(cache.get("k1"));

      assert countingCS.numLoads == 1 : "Expected 1, was " + countingCS.numLoads;
      assert countingCS.numContains == 0 : "Expected 0, was " + countingCS.numContains;
   }



   public void testSkipCacheFlagUsage() throws PersistenceException {
      CountingStore countingCS = getCountingCacheStore();

      store.write(MarshalledEntryUtil.create("k1", "v1", cache));

      assert countingCS.numLoads == 0;
      assert countingCS.numContains == 0;
      //load using SKIP_CACHE_LOAD should not find the object in the store
      assert cache.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD).get("k1") == null;
      assert countingCS.numLoads == 0;
      assert countingCS.numContains == 0;

      // counter-verify that the object was actually in the store:
      assert "v1".equals(cache.get("k1"));
      assert countingCS.numLoads == 1 : "Expected 1, was " + countingCS.numLoads;
      assert countingCS.numContains == 0 : "Expected 0, was " + countingCS.numContains;

      // now check that put won't return the stored value
      store.write(MarshalledEntryUtil.create("k2", "v2", cache));
      Object putReturn = cache.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD).put("k2", "v2-second");
      assert putReturn == null;
      assert countingCS.numLoads == 1 : "Expected 1, was " + countingCS.numLoads;
      assert countingCS.numContains == 0 : "Expected 0, was " + countingCS.numContains;
      // but it inserted it in the cache:
      assert "v2-second".equals(cache.get("k2"));
      // perform the put in the cache & store, using same value:
      putReturn = cache.put("k2", "v2-second");
      //returned value from the cache:
      assert "v2-second".equals(putReturn);
      //and verify that the put operation updated the store too:
      InvocationContextFactory icf = TestingUtil.extractComponent(cache, InvocationContextFactory.class);
      InvocationContext context = icf.createSingleKeyNonTxInvocationContext();
      assert "v2-second".equals(CompletionStages.join(persistenceManager.loadFromAllStores("k2", context.isOriginLocal(), true)).getValue());
      assertEquals(countingCS.numLoads,2, "Expected 2, was " + countingCS.numLoads);

      assert countingCS.numContains == 0 : "Expected 0, was " + countingCS.numContains;
      cache.containsKey("k1");
      assert countingCS.numContains == 0 : "Expected 0, was " + countingCS.numContains;
      assert !cache.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD).containsKey("k3");
      assert countingCS.numContains == 0 : "Expected 0, was " + countingCS.numContains;
      assert countingCS.numLoads == 2 : "Expected 2, was " + countingCS.numLoads;

      //now with batching:
      boolean batchStarted = cache.getAdvancedCache().startBatch();
      assert batchStarted;
      assert null == cache.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD).get("k1batch");
      assert countingCS.numLoads == 2 : "Expected 2, was " + countingCS.numLoads;
      assert null == cache.getAdvancedCache().get("k2batch");
      assert countingCS.numLoads == 3 : "Expected 3, was " + countingCS.numLoads;
      cache.endBatch(true);
   }

   private CountingStore getCountingCacheStore() {
      CountingStore countingCS = TestingUtil.getFirstLoader(cache);
      reset(cache, countingCS);
      return countingCS;
   }

   public void testSkipCacheLoadFlagUsage() throws PersistenceException {
      CountingStore countingCS = getCountingCacheStore();

      TestObjectStreamMarshaller sm = new TestObjectStreamMarshaller();
      try {
         store.write(MarshalledEntryUtil.create("home", "Vermezzo", sm));
         store.write(MarshalledEntryUtil.create("home-second", "Newcastle Upon Tyne", sm));

         assert countingCS.numLoads == 0;
         //load using SKIP_CACHE_LOAD should not find the object in the store
         assert cache.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD).get("home") == null;
         assert countingCS.numLoads == 0;

         assert cache.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD).put("home", "Newcastle") == null;
         assert countingCS.numLoads == 0;

         final Object put = cache.getAdvancedCache().put("home-second", "Newcastle Upon Tyne, second");
         assertEquals(put, "Newcastle Upon Tyne");
         assert countingCS.numLoads == 1;
      } finally {
         sm.stop();
      }
   }

   private void reset(Cache<?, ?> cache, CountingStore countingCS) {
      cache.clear();
      countingCS.numLoads = 0;
      countingCS.numContains = 0;
   }

   public static class CountingStore implements SegmentedAdvancedLoadWriteStore {
      public int numLoads, numContains;

      @Override
      public int size() {
         return 0;
      }


      @Override
      public void clear() {

      }

      @Override
      public void purge(Executor threadPool, PurgeListener task) {

      }

      @Override
      public void init(InitializationContext ctx) {

      }

      @Override
      public void write(MarshallableEntry entry) {

      }

      @Override
      public boolean delete(Object key) {
         return false;
      }


      @Override
      public MarshallableEntry loadEntry(Object key) throws PersistenceException {
         incrementLoads();
         return null;
      }

      @Override
      public Publisher<MarshallableEntry> entryPublisher(Predicate filter, boolean fetchValue, boolean fetchMetadata) {
         return null;
      }

      @Override
      public MarshallableEntry get(int segment, Object key) {
         return loadEntry(key);
      }

      @Override
      public boolean contains(int segment, Object key) {
         return contains(key);
      }

      @Override
      public void write(int segment, MarshallableEntry entry) {
      }

      @Override
      public boolean delete(int segment, Object key) {
         return false;
      }

      @Override
      public Publisher<MarshallableEntry> entryPublisher(IntSet segments, Predicate filter, boolean fetchValue, boolean fetchMetadata) {
         return null;
      }

      @Override
      public void start() {
      }

      @Override
      public void stop() {
      }

      @Override
      public boolean contains(Object key) throws PersistenceException {
         numContains++;
         return false;
      }

      private void incrementLoads() {
         numLoads++;
      }

      @Override
      public int size(IntSet segments) {
         return 0;
      }

      @Override
      public Publisher publishKeys(IntSet segments, Predicate filter) {
         return null;
      }

      @Override
      public void clear(IntSet segments) {

      }
   }

   @BuiltBy(CountingStoreConfigurationBuilder.class)
   @ConfigurationFor(CountingStore.class)
   public static class CountingStoreConfiguration extends AbstractStoreConfiguration<CountingStoreConfiguration> {

      public CountingStoreConfiguration(AttributeSet attributes, AsyncStoreConfiguration async) {
         super(Element.DUMMY_STORE, attributes, async);
      }
   }

   public static class CountingStoreConfigurationBuilder extends AbstractStoreConfigurationBuilder<CountingStoreConfiguration, CountingStoreConfigurationBuilder> {

      public CountingStoreConfigurationBuilder(PersistenceConfigurationBuilder builder) {
         super(builder, CountingStoreConfiguration.attributeDefinitionSet());
      }

      @Override
      public CountingStoreConfiguration create() {
         return new CountingStoreConfiguration(attributes.protect(), async.create());
      }

      @Override
      public CountingStoreConfigurationBuilder self() {
         return this;
      }

   }
}
