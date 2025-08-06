# 📦 Package Structure

| Package              | Description                                    |
|----------------------|------------------------------------------------|
| `config`             | Configuration (CORS, Swagger, JWT, Redis)     |
| `constants`         |  Constants values                             |
| `controller`         | REST controllers for OTP and User operations   |
| `dto`                | Request/response payloads for service class      |                      |
| `exception`          | Custom exceptions and global handlers          |
| `http`                 |  Common handler to hit the external api      |
| `mapper`            | model mapper to convert the pojo to dto or dto to pojo| 
| `model`              | JPA entities                                   |
| `pojo`               | classes to handle request and response in the controller|
| `repository`         | DB interaction via Spring Data JPA             |
| `security`           | JWT filters, authentication manager            |
| `service.inter`            | Interfaces for business logic                  |
| `service.impl`       | Implementation of services                     |
| `util`               | Helper classes (token builder, validators)     |

---