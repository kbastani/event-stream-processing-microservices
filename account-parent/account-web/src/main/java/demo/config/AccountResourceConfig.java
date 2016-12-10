package demo.config;

import demo.account.Account;
import demo.account.AccountController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Configuration
public class AccountResourceConfig {

    /**
     * Enriches the {@link Account} resource with hypermedia links.
     *
     * @return a hypermedia processor for the {@link Account} resource
     */
    @Bean
    public ResourceProcessor<Resource<Account>> accountProcessor() {
        return new ResourceProcessor<Resource<Account>>() {
            @Override
            public Resource<Account> process(Resource<Account> resource) {
                resource.add(
                        linkTo(AccountController.class)
                                .slash("accounts")
                                .slash(resource.getContent().getAccountId())
                                .slash("commands")
                                .withRel("commands"));
                return resource;
            }
        };
    }

}
