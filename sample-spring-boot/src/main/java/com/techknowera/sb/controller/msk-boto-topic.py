import boto3
from confluent_kafka.admin import AdminClient, NewTopic

# AWS Configuration
AWS_REGION = "us-east-1"  # Change to your MSK region
CLUSTER_ARN = "arn:aws:kafka:us-east-1:123456789012:cluster/my-msk-cluster/abcd1234"  # Update with your MSK ARN
TOPIC_NAME = "my-msk-topic"

# Step 1: Get Bootstrap Servers
def get_bootstrap_servers():
    client = boto3.client("kafka", region_name=AWS_REGION)
    
    try:
        response = client.get_bootstrap_brokers(ClusterArn=CLUSTER_ARN)
        bootstrap_servers = response.get("BootstrapBrokerStringTls")  # Use TLS brokers
        if not bootstrap_servers:
            raise Exception("No bootstrap servers found!")
        return bootstrap_servers
    except Exception as e:
        print(f"❌ Error retrieving bootstrap servers: {e}")
        return None

# Step 2: Create Kafka Topic
def create_kafka_topic(bootstrap_servers, topic_name):
    conf = {"bootstrap.servers": bootstrap_servers}  # MSK Serverless requires TLS
    admin_client = AdminClient(conf)

    topic_list = [NewTopic(topic_name, num_partitions=1, replication_factor=1)]

    # Check if the topic already exists
    existing_topics = admin_client.list_topics(timeout=5).topics
    if topic_name in existing_topics:
        print(f"✅ Topic '{topic_name}' already exists!")
        return

    # Create the topic
    future = admin_client.create_topics(topic_list)
    
    for topic, future_result in future.items():
        try:
            future_result.result()  # Block until the operation completes
            print(f"✅ Successfully created topic: {topic}")
        except Exception as e:
            print(f"❌ Failed to create topic {topic}: {e}")

# Run the script
bootstrap_servers = get_bootstrap_servers()
if bootstrap_servers:
    create_kafka_topic(bootstrap_servers, TOPIC_NAME)
