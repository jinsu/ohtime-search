## OH time search
A search service for OH time CRUD app that hooks into solr for search.

* _spray-can_, Scala 2.10 + Akka 2.2 + spray 1.2 (the `on_spray-can_1.2` branch)

To run:

1. Launch SBT:

        $ sbt

2. Compile everything and run all tests:

        > test

3. Start the application:

        > re-start

4. Browse to http://localhost:8080/

5. Stop the application:

        > re-stop
