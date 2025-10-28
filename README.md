# payment-service
To run the project, create a `.env` file in the root directory with the following content:

## Environment Configuration
```env
DB_URL=jdbc:postgresql://localhost:5432/auctionDB
DB_USERNAME=your_postgres_username
DB_PASSWORD=your_postgres_password

Then copy the example config:
cp src/main/resources/application.properties.example src/main/resources/application.properties

and make sure you have PostgreSQL running locally with the correct credentials.