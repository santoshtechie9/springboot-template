import boto3

# Initialize the Boto3 client for MSK
client = boto3.client('kafka', region_name='us-east-1')  # Change the region as needed

def get_cluster_status(cluster_name):
    try:
        # Fetch the list of all MSK clusters
        response = client.list_clusters(
            ClusterType='ALL'  # Fetch both provisioned and serverless clusters
        )

        for cluster in response.get('ClusterInfoList', []):
            if cluster['ClusterName'] == cluster_name:
                cluster_arn = cluster['ClusterArn']
                
                # Get detailed cluster information
                cluster_details = client.describe_cluster(ClusterArn=cluster_arn)
                cluster_state = cluster_details['ClusterInfo']['State']
                
                print(f"Cluster Name: {cluster_name}")
                print(f"Cluster ARN: {cluster_arn}")
                print(f"Cluster State: {cluster_state}")
                return cluster_state

        print(f"❌ Cluster '{cluster_name}' not found.")
        return None

    except Exception as e:
        print(f"⚠️ Error fetching cluster status: {str(e)}")
        return None

# Replace with your MSK cluster name
cluster_name = "my-msk-cluster"
get_cluster_status(cluster_name)
