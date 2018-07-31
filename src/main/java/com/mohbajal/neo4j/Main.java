package com.mohbajal.neo4j;


import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.kernel.configuration.BoltConnector;
import org.neo4j.kernel.configuration.Settings;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {


    public static void main(String[] args) {
        GraphDatabaseService graphDb;
        Node firstNode;
        Node secondNode;
        Relationship relationship;


        File databaseDirectory = new File("data");
        BoltConnector bolt = new BoltConnector("0");
        Path path = Paths.get("conf/neo4j.conf");

        graphDb = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(databaseDirectory)
                .setConfig(GraphDatabaseSettings.pagecache_memory, "512M")
                .setConfig(GraphDatabaseSettings.string_block_size, "60")
                .setConfig(GraphDatabaseSettings.array_block_size, "300")
                .setConfig(GraphDatabaseSettings.store_internal_log_level, "DEBUG")
                .loadPropertiesFromFile(path.toAbsolutePath().toString())
                .setConfig(bolt.enabled, "true")
                .setConfig(bolt.type, "BOLT")
                .setConfig(bolt.listen_address, "0.0.0.0:7687")
                .setConfig(bolt.encryption_level, BoltConnector.EncryptionLevel.OPTIONAL.toString())
                .newGraphDatabase();

        try ( Transaction tx = graphDb.beginTx() ) {
            firstNode = graphDb.createNode();
            firstNode.setProperty( "message", "Hello, " );
            secondNode = graphDb.createNode();
            secondNode.setProperty( "message", "World!" );

            relationship = firstNode.createRelationshipTo( secondNode, RelTypes.KNOWS );
            relationship.setProperty( "message", "brave Neo4j " );

            System.out.print( firstNode.getProperty( "message" ) );
            System.out.print( relationship.getProperty( "message" ) );
            System.out.print( secondNode.getProperty( "message" ) );
            tx.success();
        }

        registerShutdownHook( graphDb );
    }


    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }

    private enum RelTypes implements RelationshipType
    {
        KNOWS
    }
}
