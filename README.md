"# scala-advertcampaignbidder" 
There are several ways to run this application. You could:
1. Clone this repository then run it directly by navigating to root directory of the application and running the *startapp.cmd* script for Windows or  simply by running *sbt run* for all  operating system types
2. Build a container image using the Docker file in the root directory and running the container
3. Heroku
### Direct run
1. clone this repository with this command: git clone https://github.com/neoandrey/scala-advertcampaignbidder.git. 
2. Navigate to the root directory of the clone repository.
3. Type *sbt run* or run the *startapp.cmd* script if you are working with a Windows machine.

### Docker
1. Download the Dockerfile from the repository to a suitable location on your machine.
2. Navigate to the location of the downloaded Dockerfile
3. Run the following commmand to build the container image:
        sudo docker build  --build-arg APP_PORT=2212  --build-arg REPO_EXTRACT="scala-advertcampaignbidder" --build-arg RUN_COMMNAND='cd /opt/scala/$APP_NAME/ && sudo sbt run'  --build-arg START_TYPE=start --build-arg APP_NAME=scala-advertcampaignbidder --build-arg APP_REPO="https://github.com/neoandrey/scala-advertcampaignbidder.git"  --build-arg OPENJDK_TAG="8u151-jre-alpine" -t advert_campaign_bidding_app . --no-cache 
4. Start the container with the following command:
        sudo docker run  -p 2212:2212 -d advert_campaign_bidding_app
5. Alternatively, you could pull the container image:
        sudo docker pull neoandrey/advert_campaign_bidding_app
6. Then run with:
        sudo docker run  -p 2212:2212 -d neoandrey/advert_campaign_bidding_app

### Heroku
The application has also been deployed to heroku and can be access here [advert_campaign_bidding_app](https://advercampaignbidder.herokuapp.com/)

### Testing
1. clone this repository with this command: git clone https://github.com/neoandrey/scala-advertcampaignbidder.git. 
2. Navigate to the root directory of the clone repository.
3. Type *sbt test* or run the *test.cmd* script if you are working with a Windows machine.
