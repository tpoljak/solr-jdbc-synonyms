package com.s24.search.solr.analysis.jdbc;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymFilterFactory;
import org.apache.lucene.analysis.util.ResourceLoader;

/**
 * Factory for {@link SynonymFilter} which loads synonyms from a database.
 */
public class JdbcSynonymFilterFactory extends SynonymFilterFactory {
   /**
    * Database based reader.
    */
   private final JdbcReader reader;

   private static ThreadLocal<JndiJdbcReader> transfer = new ThreadLocal<>();

   /**
    * Constructor.
    * 
    * @param args
    *           Configuration.
    */
   public JdbcSynonymFilterFactory(Map<String, String> args) {
      super(readArguments(args));
      reader = transfer.get();
      transfer.remove();
   }

   /**
    * Set synonyms file to one fixed name. This is needed because our patched
    * resource loader should load the synonyms exactly once.
    * 
    * @param args
    *           Configuration.
    * @return Configuration.
    */
   private static Map<String, String> readArguments(Map<String, String> args) {
      args.put("synonyms", "database");

      String name = args.remove("jndiName");
      String sql = args.remove("sql");
      transfer.set(new JndiJdbcReader(name, sql));
      return args;
   }

   @Override
   public void inform(ResourceLoader loader) throws IOException {
      super.inform(new JdbcResourceLoader(loader, reader));
   }
}
