# Checklist for Review: i-love-shopping (1/3)

## Project Documentation

* [ ]  The README file contains a clear project overview, ERD, setup instructions, and usage guide

## Authentication and Security

* [ ]  The platform implements a Business-to-Consumer (B2C) e-commerce model
* [ ]  The system supports both email-password and OAuth authentication methods
* [ ]  CAPTCHA is integrated into the registration process
* [ ]  The student can explain the concept of JWT and its components (header, payload, signature)
* [ ]  Access tokens are stored in memory
* [ ]  Refresh token rotation is implemented with single-use validation
* [ ]  Each refresh token can only be used once; a new token is issued with each refresh
* [ ]  Old refresh tokens are rejected
* [ ]  A token revocation mechanism is in place for both access and refresh tokens
* [ ]  Password recovery and reset functionality via email is implemented
* [ ]  Two-factor authentication (2FA) is available as an optional user-enabled feature
* [ ]  User input validation is implemented on both client and server sides for authentication forms

## Database and Scalability

* [ ]  The student can explain the scalability features of the chosen database and how they support potential growth of the platform
* [ ]  The student can explain ACID properties and their importance in e-commerce database design
* [ ]  An ERD is provided, clearly showing entities, attributes, relationships, primary keys, foreign keys, cardinality, and modality

## Product Catalog

* [ ]  The student can demonstrate and explain the search implementation including database design and basic text search functionality
* [ ]  The product data model includes all required fields: id, name, description, price, stock quantity, category, brand, images, weight/dimensions (metric and imperial)
* [ ]  Products are organized into categories with an intuitive browsing structure
* [ ]  Faceted search is implemented, allowing users to refine results by attributes (e.g., price range, brand, category)
* [ ]  Product listing includes sorting options: relevance, price, and rating
* [ ]  Product images are stored with proper file handling and basic serving functionality

## Testing

* [ ]  The student can explain their approach to testing, including the use of automated and manual tests
* [ ]  Automated tests exist for Unit, API integration, and Security, covering authentication and product catalog functionality
* [ ]  Ask the student to explain and demonstrate the functionality of the tests

## Architecture

* [ ]  The student can explain their chosen architectural approach and justify how it aligns with the platform’s scalability requirements

## Extra Evaluation

* [ ]  Quality of authentication system implementation, security measures, and user experience
* [ ]  Database design quality, ERD completeness, and adherence to ACID properties
* [ ]  Product catalog organization, search functionality, and filtering system implementation
