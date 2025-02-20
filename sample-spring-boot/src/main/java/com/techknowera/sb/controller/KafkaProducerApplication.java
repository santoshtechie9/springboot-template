@SpringBootApplication
public class ProducerApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(ProducerApplication.class, args);
        KafkaProducerService producer = context.getBean(KafkaProducerService.class);
        
        // Sending test messages
        producer.sendMessage("🔥 MSK IAM Authentication Test Message");
    }
}

