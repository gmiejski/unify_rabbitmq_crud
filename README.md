# unify_rabbitmq_crud


## Setup

* `npm install -g wait-port`
* prepare files for Kafka:
    * create 3 config files same way as they do this here:
        * https://kafka.apache.org/quickstart
        * bind ports to `9092`, `9093` and `9094`
* start Kafka        
    * `KAFKA_PATH=${your_main_kafka_path} make setup-clusters`
        * sometimes something goes wrong - just run this command second time, and you should see all connections successful
    * `KAFKA_PATH=${your_main_kafka_path} make topics_setup`
    * verify everything works correctly: `KAFKA_PATH=${your_main_kafka_path} make list_topics` - shoule list 4 topics
* start rabbitMQ:  
    * install `brew update` && `brew install rabbitmq`
    * in case of linking errors: 
        * `sudo mkdir /usr/local/sbin`
        * `sudo chown -R $(whoami) $(brew --prefix)/*`
        * `brew link rabbitmq`
    * `rabbitmq-server` and go to `http://localhost:15672/#/exchanges`
* install kafka-connect:
    * Download confluent platform `https://www.confluent.io/download/`
    * `.../confluent-5.0.0/bin/connect-standalone connectors/worker-working.properties connectors/create-questions.properties connectors/update-questions.properties connectors/delete-questions.properties`


## kill everything

* `make down`
* `make kill_rabbit`

## Run integration-consistenty test

[Docs](src/integration/README.md)






## Other sources - kafka streams docs

* https://kafka.apache.org/20/documentation/streams/quickstart
* https://kafka.apache.org/20/documentation/streams/tutorial
* https://kafka.apache.org/20/documentation/streams/developer-guide/interactive-queries.html#querying-local-state-stores-for-an-app-instance

* https://kafka.apache.org/20/documentation/streams/developer-guide/testing.html
* https://stackoverflow.com/questions/46220663/kstream-ktable-join-writing-to-the-ktable-how-to-sync-the-join-with-the-ktable
* http://bigdatums.net/2017/05/21/send-key-value-messages-kafka-console-producer/
* https://docs.confluent.io/3.1.1/streams/index.html
* https://kafka.apache.org/10/documentation/streams/developer-guide/interactive-queries.html
* https://docs.confluent.io/current/streams/concepts.html
* https://docs.confluent.io/current/streams/architecture.html?_ga=2.265777096.696249922.1537392407-607151404.1536876543#state
* https://docs.confluent.io/current/streams/kafka-streams-examples/docs/index.html?_ga=2.94790747.696249922.1537392407-607151404.1536876543

### Kafka connect:

* Install - https://docs.confluent.io/current/connect/managing/confluent-hub/client.html#confluent-hub-client
* confluent-hub install confluentinc/kafka-connect-rabbitmq:latest
    * this produced error with java :D, but finished
    * install to default folder `/usr/local/share/confluent-hub-components`
* fucking errors
    * `ERROR Consumer io.confluent.connect.rabbitmq.ConnectConsumer@418f9617 (amq.ctag-4HDwpynLDdEGZN7L1vif1A) method handleDelivery for channel AMQChannel(amqp://guest@127.0.0.1:5672/,1) threw an exception for channel AMQChannel(amqp://guest@127.0.0.1:5672/,1) (com.rabbitmq.client.impl.ForgivingExceptionHandler:124)
      java.lang.NullPointerException`
*
 ```~/programming/confluent-5.0.0/bin/connect-standalone connectors/worker2.properties connectors/create-questions.properties```
* transforms must go into `connector.properties`:
    * `bin/connect-standalone worker.properties connector1.properties [connector2.properties connector3.properties ...]`
* 

### might be usefull
* https://www.programcreek.com/java-api-examples/?code=jcustenborder/kafka-connect-rabbitmq/kafka-connect-rabbitmq-master/src/main/java/com/github/jcustenborder/kafka/connect/rabbitmq/MessageConverter.java#

## TODOs for MVP

* Objects playground
    * [x] Create test that will check for data consistency
    * [x] Make 3 channels and pass them to single one with proper serdes
    
* Questions
    * [x] Implement single channel aggregation with test
    * [x] Make sure data is consistent
    * [x] Join 3 CRUD topics
    * [x] Connect RabbitMQ to kafka - https://docs.confluent.io/current/connect/kafka-connect-rabbitmq/rabbit_m_q_source_connector_config.html
        * [x] <s>run rabbitMQ on docker</s> | run rabbitmq locally
        * [x] create producer to rabbitMQ
        * [x] use kafka-connect to create topic in kafka with events
        * [x] make CRUD topics operate on original question events (those coming from legacy systems)
        * [x] update map operations, to change original, to GenericField events
        * [x] write integration test
 
* Prod consistency test:
    * [ ] prepare rabbitMQ to be run via docker
    * [ ] prepare kafka to be run in docker
    * [ ] prepare data in RabbitMQ, persist expected state
    * [ ] start kafka connectors and kafka stream
    * [ ] Verify results

* Setup local stores with REST API

* verify solution
    * [ ] https://stackoverflow.com/users/7897191/michal-borowiecki
    
* Prod performance test:
    * [ ] TODO fill this up

     
## Things to check at the end:
* [ ] make sure topics are partitioned by question id
