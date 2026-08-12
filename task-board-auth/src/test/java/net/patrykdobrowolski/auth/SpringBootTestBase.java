package net.patrykdobrowolski.auth;

import net.patrykdobrowolski.auth.service.FacebookAuthenticationProvider;
import net.patrykdobrowolski.auth.service.GithubAuthenticationProvider;
import net.patrykdobrowolski.auth.service.GoogleAuthenticationProvider;
import net.patrykdobrowolski.auth.service.LinkedInAuthenticationProvider;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
public class SpringBootTestBase {

    @MockitoBean
    protected GoogleAuthenticationProvider googleAuthenticationProvider;

    @MockitoBean
    protected GithubAuthenticationProvider githubAuthenticationProvider;

    @MockitoBean
    protected FacebookAuthenticationProvider facebookAuthenticationProvider;

    @MockitoBean
    protected LinkedInAuthenticationProvider linkedInAuthenticationProvider;
}
