@SpringBootApplication
public class ProducerApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(ProducerApplication.class, args);
        KafkaProducerService producer = context.getBean(KafkaProducerService.class);
        
        // Sending test messages
        producer.sendMessage("🔥 MSK IAM Authentication Test Message");
    }
}


package com.example.msk;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.AWSStaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.BasicAWSCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kafka.AWSKafka;
import software.amazon.awssdk.services.kafka.model.ListClustersRequest;
import software.amazon.awssdk.services.kafka.model.ListClustersResponse;
import software.amazon.awssdk.services.kafka.model.ClusterInfo;

@SpringBootApplication
public class AWSKafkaApplication {

    private static final Logger logger = LoggerFactory.getLogger(AWSKafkaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AWSKafkaApplication.class, args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            // Step 1: Initialize AWS MSK Client with IAM credentials (using static credentials here for demonstration)
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials("your-access-key-id", "your-secret-access-key");

            AWSKafka kafkaClient = AWSKafka.builder()
                    .credentialsProvider(new AWSStaticCredentialsProvider(awsCredentials))
                    .region(Region.of("us-east-1"))  // Change to your MSK region
                    .build();

            // Step 2: List MSK Clusters
            listMSKClusters(kafkaClient);
        };
    }

    /**
     * List MSK Clusters from the AWS Kafka service.
     * 
     * @param kafkaClient The AWSKafka client initialized with credentials.
     */
    private void listMSKClusters(AWSKafka kafkaClient) {
        try {
            ListClustersRequest request = ListClustersRequest.builder().build();
            ListClustersResponse response = kafkaClient.listClusters(request);

            // Print out the names of the MSK clusters
            if (response.clusterInfoList() != null && !response.clusterInfoList().isEmpty()) {
                logger.info("MSK Clusters found:");
                for (ClusterInfo cluster : response.clusterInfoList()) {
                    logger.info("Cluster Name: " + cluster.clusterName());
                }
            } else {
                logger.warn("No MSK clusters found in the specified region.");
            }
        } catch (Exception e) {
            logger.error("Failed to list MSK clusters: ", e);
        }
    }
}
