import boto3
from kafka import KafkaAdminClient
from kafka.admin import NewTopic
from botocore.exceptions import NoCredentialsError

# AWS Configuration
AWS_REGION = "us-east-1"  # Update to your region
CLUSTER_ARN = "arn:aws:kafka:us-east-1:123456789012:cluster/my-msk-cluster/abcd1234"  # Update with your MSK ARN
TOPIC_NAME = "my-msk-topic"

# Step 1: Fetch Bootstrap Servers from AWS MSK
def get_bootstrap_servers():
    client = boto3.client("kafka", region_name=AWS_REGION)
    try:
        response = client.get_bootstrap_brokers(ClusterArn=CLUSTER_ARN)
        bootstrap_servers = response.get("BootstrapBrokerStringTls")  # MSK Serverless requires TLS
        if not bootstrap_servers:
            raise Exception("No bootstrap servers found!")
        return bootstrap_servers
    except NoCredentialsError:
        print("❌ AWS credentials not found. Set them using environment variables or IAM roles.")
        return None
    except Exception as e:
        print(f"❌ Error retrieving bootstrap servers: {e}")
        return None

# Step 2: Create Kafka Topic
def create_kafka_topic(bootstrap_servers, topic_name):
    try:
        admin_client = KafkaAdminClient(bootstrap_servers=bootstrap_servers, security_protocol="SASL_SSL")

        # Check if topic already exists
        existing_topics = admin_client.list_topics()
        if topic_name in existing_topics:
            print(f"✅ Topic '{topic_name}' already exists!")
            return

        # Create the topic
        topic_list = [NewTopic(name=topic_name, num_partitions=1, replication_factor=1)]
        admin_client.create_topics(new_topics=topic_list)
        print(f"✅ Successfully created topic: {topic_name}")

    except Exception as e:
        print(f"❌ Failed to create topic {topic_name}: {e}")

# Run the script
bootstrap_servers = get_bootstrap_servers()
if bootstrap_servers:
    create_kafka_topic(bootstrap_servers, TOPIC_NAME)
