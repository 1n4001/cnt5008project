# cnt5008project

Project is investigating both ACK and NACK multicast traffic to create a reliable UDP multicast data stream.

# Compiling

Project uses maven and java 8

To compile: mvn package

# Running

The package target creates a shaded jar with all of the dependencies added in.

For example:

java -cp target/jdavies-cnt5008-all.jar edu.ucf.student.jdavies.cnt5008.Main -usecase OneToMany -mode ACK -loss 00.01 -receivers 10
