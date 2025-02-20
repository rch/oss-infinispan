package org.infinispan.server.core.dataconversion;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.infinispan.commons.dataconversion.MediaType.APPLICATION_OBJECT;
import static org.infinispan.commons.dataconversion.MediaType.APPLICATION_WWW_FORM_URLENCODED;
import static org.infinispan.commons.dataconversion.MediaType.APPLICATION_XML;
import static org.infinispan.commons.dataconversion.MediaType.TEXT_PLAIN;
import static org.testng.AssertJUnit.assertArrayEquals;
import static org.testng.AssertJUnit.assertEquals;

import org.infinispan.commons.configuration.ClassAllowList;
import org.infinispan.test.data.Address;
import org.infinispan.test.data.Person;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "functional", testName = "server.XMLTranscoderTest")
public class XMLTranscoderTest {

   private Person person;
   private XMLTranscoder xmlTranscoder = new XMLTranscoder(new ClassAllowList(singletonList(".*")));

   @BeforeClass(alwaysRun = true)
   public void setUp() {
      person = new Person("Joe");
      Address address = new Address();
      address.setCity("London");
      person.setAddress(address);
   }

   public void testObjectToXML() {
      String xmlString = new String((byte[]) xmlTranscoder.transcode(person, APPLICATION_OBJECT, APPLICATION_XML));
      Object transcodedBack = xmlTranscoder.transcode(xmlString, APPLICATION_XML, APPLICATION_OBJECT);
      assertEquals("Must be an equal objects", person, transcodedBack);
   }

   @Test
   public void testWWWFormUrlEncoded() {
      byte[] transcoded = (byte[]) xmlTranscoder.transcode("%3Cstring%3EHello%20World%21%3C%2Fstring%3E", APPLICATION_WWW_FORM_URLENCODED, APPLICATION_XML);
      assertEquals("<string>Hello World!</string>", new String(transcoded));
   }

   public void testTextToXML() {
      byte[] value = "Hello World!".getBytes(UTF_8);

      Object asXML = xmlTranscoder.transcode(value, TEXT_PLAIN, APPLICATION_XML);
      assertEquals("<string>Hello World!</string>", new String((byte[]) asXML));

      Object xmlAsText = xmlTranscoder.transcode(asXML, APPLICATION_XML, TEXT_PLAIN);
      assertArrayEquals("<string>Hello World!</string>".getBytes(), (byte[]) xmlAsText);
   }
}
