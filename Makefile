setup-clusters:
	${KAFKA_PATH}/bin/zookeeper-server-start.sh ${KAFKA_PATH}/config/zookeeper.properties &
	wait-port localhost:2181 -t 10000
	${KAFKA_PATH}/bin/kafka-server-start.sh ${KAFKA_PATH}/config/server.properties &
	${KAFKA_PATH}/bin/kafka-server-start.sh ${KAFKA_PATH}/config/server-1.properties &
	${KAFKA_PATH}/bin/kafka-server-start.sh ${KAFKA_PATH}/config/server-2.properties &
	wait-port localhost:9092 -t 10000
	wait-port localhost:9093 -t 10000
	wait-port localhost:9094 -t 10000

down:
	ps -ef | grep 'kafka' | grep -v grep | grep -v zookeeper |  awk '{print $$2}' | xargs  kill -9
	ps -ef | grep 'zookeeper' | grep -v grep | awk '{print $$2}' | xargs  kill -9
