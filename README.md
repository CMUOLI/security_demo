# User IAM Federation & Security Demo

Securing microservices with Keycloak in an Identity and Access Management proof of concept project.

To build the project locally, first install [Docker](https://docs.docker.com/engine/installation/).

After a successful installation of docker, run the following:

```sh
git clone git@github.com:CMUOLI/security_demo.git
cd security_demo
docker-compose build
docker-compose up
```

You may have to create a directory named `/keycloak/db` in the root of your local drive to adhere to our compose file.

---

***Authors***
- [CMU OLI](https://github.com/CMUOLI)
- [CMUCC](https://github.com/cloudcomputingcourse)
