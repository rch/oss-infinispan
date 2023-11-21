package org.infinispan.test.hibernate.cache.v62.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Arrays;

/**
 * @since 15.0
 **/
public class ProxyStatement implements Statement {
   private final int n;
   private final Statement delegate;

   ProxyStatement(int n, Statement delegate) {
      this.n = n;
      this.delegate = delegate;
   }

   @Override
   public ResultSet executeQuery(String sql) throws SQLException {
      System.out.printf("%d executeQuery(%s)%n", n, sql);
      return delegate.executeQuery(sql);
   }

   @Override
   public int executeUpdate(String sql) throws SQLException {
      System.out.printf("%d executeUpdate(%s)%n", n, sql);
      return delegate.executeUpdate(sql);
   }

   @Override
   public void close() throws SQLException {
      System.out.printf("%d close()%n", n);
      delegate.close();
   }

   @Override
   public int getMaxFieldSize() throws SQLException {
      return delegate.getMaxFieldSize();
   }

   @Override
   public void setMaxFieldSize(int max) throws SQLException {
      delegate.setMaxFieldSize(max);
   }

   @Override
   public int getMaxRows() throws SQLException {
      return delegate.getMaxRows();
   }

   @Override
   public void setMaxRows(int max) throws SQLException {
      delegate.setMaxRows(max);
   }

   @Override
   public void setEscapeProcessing(boolean enable) throws SQLException {
      delegate.setEscapeProcessing(enable);
   }

   @Override
   public int getQueryTimeout() throws SQLException {
      return delegate.getQueryTimeout();
   }

   @Override
   public void setQueryTimeout(int seconds) throws SQLException {
      delegate.setQueryTimeout(seconds);
   }

   @Override
   public void cancel() throws SQLException {
      delegate.cancel();
   }

   @Override
   public SQLWarning getWarnings() throws SQLException {
      return delegate.getWarnings();
   }

   @Override
   public void clearWarnings() throws SQLException {
      delegate.clearWarnings();
   }

   @Override
   public void setCursorName(String name) throws SQLException {
      delegate.setCursorName(name);
   }

   @Override
   public boolean execute(String sql) throws SQLException {
      System.out.printf("%d execute(%s)%n", n, sql);
      return delegate.execute(sql);
   }

   @Override
   public ResultSet getResultSet() throws SQLException {
      return delegate.getResultSet();
   }

   @Override
   public int getUpdateCount() throws SQLException {
      return delegate.getUpdateCount();
   }

   @Override
   public boolean getMoreResults() throws SQLException {
      return delegate.getMoreResults();
   }

   @Override
   public void setFetchDirection(int direction) throws SQLException {
      delegate.setFetchDirection(direction);
   }

   @Override
   public int getFetchDirection() throws SQLException {
      return delegate.getFetchDirection();
   }

   @Override
   public void setFetchSize(int rows) throws SQLException {
      delegate.setFetchSize(rows);
   }

   @Override
   public int getFetchSize() throws SQLException {
      return delegate.getFetchSize();
   }

   @Override
   public int getResultSetConcurrency() throws SQLException {
      return delegate.getResultSetConcurrency();
   }

   @Override
   public int getResultSetType() throws SQLException {
      return delegate.getResultSetType();
   }

   @Override
   public void addBatch(String sql) throws SQLException {
      System.out.printf("%d addBatch(%s)%n", n, sql);
      delegate.addBatch(sql);
   }

   @Override
   public void clearBatch() throws SQLException {
      delegate.clearBatch();
   }

   @Override
   public int[] executeBatch() throws SQLException {
      return delegate.executeBatch();
   }

   @Override
   public Connection getConnection() throws SQLException {
      return delegate.getConnection();
   }

   @Override
   public boolean getMoreResults(int current) throws SQLException {
      return delegate.getMoreResults(current);
   }

   @Override
   public ResultSet getGeneratedKeys() throws SQLException {
      return delegate.getGeneratedKeys();
   }

   @Override
   public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      System.out.printf("%d executeUpdate(%s, %d)%n", n, sql, autoGeneratedKeys);
      return delegate.executeUpdate(sql, autoGeneratedKeys);
   }

   @Override
   public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
      System.out.printf("%d executeUpdate(%s, %s)%n", n, sql, Arrays.toString(columnIndexes));
      return delegate.executeUpdate(sql, columnIndexes);
   }

   @Override
   public int executeUpdate(String sql, String[] columnNames) throws SQLException {
      System.out.printf("%d executeUpdate(%s, %s)%n", n, sql, Arrays.toString(columnNames));
      return delegate.executeUpdate(sql, columnNames);
   }

   @Override
   public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
      System.out.printf("%d execute(%s, %d)%n", n, sql, autoGeneratedKeys);
      return delegate.execute(sql, autoGeneratedKeys);
   }

   @Override
   public boolean execute(String sql, int[] columnIndexes) throws SQLException {
      System.out.printf("%d execute(%s, %s)%n", n, sql, Arrays.toString(columnIndexes));
      return delegate.execute(sql, columnIndexes);
   }

   @Override
   public boolean execute(String sql, String[] columnNames) throws SQLException {
      System.out.printf("%d execute(%s, %s)%n", n, sql, Arrays.toString(columnNames));
      return delegate.execute(sql, columnNames);
   }

   @Override
   public int getResultSetHoldability() throws SQLException {
      return delegate.getResultSetHoldability();
   }

   @Override
   public boolean isClosed() throws SQLException {
      return delegate.isClosed();
   }

   @Override
   public void setPoolable(boolean poolable) throws SQLException {
      delegate.setPoolable(poolable);
   }

   @Override
   public boolean isPoolable() throws SQLException {
      return delegate.isPoolable();
   }

   @Override
   public void closeOnCompletion() throws SQLException {
      delegate.closeOnCompletion();
   }

   @Override
   public boolean isCloseOnCompletion() throws SQLException {
      return delegate.isCloseOnCompletion();
   }

   @Override
   public long getLargeUpdateCount() throws SQLException {
      return delegate.getLargeUpdateCount();
   }

   @Override
   public void setLargeMaxRows(long max) throws SQLException {
      delegate.setLargeMaxRows(max);
   }

   @Override
   public long getLargeMaxRows() throws SQLException {
      return delegate.getLargeMaxRows();
   }

   @Override
   public long[] executeLargeBatch() throws SQLException {
      return delegate.executeLargeBatch();
   }

   @Override
   public long executeLargeUpdate(String sql) throws SQLException {
      return delegate.executeLargeUpdate(sql);
   }

   @Override
   public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      return delegate.executeLargeUpdate(sql, autoGeneratedKeys);
   }

   @Override
   public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
      return delegate.executeLargeUpdate(sql, columnIndexes);
   }

   @Override
   public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
      return delegate.executeLargeUpdate(sql, columnNames);
   }

   @Override
   public String enquoteLiteral(String val) throws SQLException {
      return delegate.enquoteLiteral(val);
   }

   @Override
   public String enquoteIdentifier(String identifier, boolean alwaysQuote) throws SQLException {
      return delegate.enquoteIdentifier(identifier, alwaysQuote);
   }

   @Override
   public boolean isSimpleIdentifier(String identifier) throws SQLException {
      return delegate.isSimpleIdentifier(identifier);
   }

   @Override
   public String enquoteNCharLiteral(String val) throws SQLException {
      return delegate.enquoteNCharLiteral(val);
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      return delegate.unwrap(iface);
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return delegate.isWrapperFor(iface);
   }
}
