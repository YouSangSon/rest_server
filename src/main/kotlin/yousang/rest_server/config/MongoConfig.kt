package yousang.rest_server.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(basePackages = ["yousang.rest_server.adapter.out.persistence.mongo"])
@EnableMongoAuditing
class MongoConfig
