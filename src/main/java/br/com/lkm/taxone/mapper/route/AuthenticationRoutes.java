package br.com.lkm.taxone.mapper.route;

import static org.apache.camel.LoggingLevel.DEBUG;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import br.com.lkm.taxone.mapper.dto.AuthenticationRequest;
import br.com.lkm.taxone.mapper.dto.AuthenticationResponse;
import br.com.lkm.taxone.mapper.dto.POCUser;
import br.com.lkm.taxone.mapper.entity.User;
import br.com.lkm.taxone.mapper.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.UserTransaction;

@ApplicationScoped
public class AuthenticationRoutes extends EndpointRouteBuilder {
    
    @Inject
    private JwtTokenUtil jwtTokenUtil;
    
    @Inject
    private EntityManager entityManager;
    
    @Inject
    private UserTransaction userTransaction;

    
    @Override
    public void configure() throws Exception {

        interceptFrom()
            .to("direct:cors");
            
        from(platformHttp("/authenticate")
            .httpMethodRestrict("POST"))
            .convertBodyTo(String.class)
            .unmarshal().json(AuthenticationRequest.class)
            .toD("jpa:?query=select u from User u where u.name = '${body.username}'")
            .log(DEBUG, ">>> body:${body}")
            .choice()
                .when(simple("${body.size} != 0"))
                    .process(e -> {
                        User u = (User)e.getIn().getBody(ArrayList.class).get(0);
                        final String token = jwtTokenUtil.generateToken(u);
                        AuthenticationResponse ar = new AuthenticationResponse(token);
                        e.getIn().setBody(ar);
                    })
                    .marshal().json()
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .endChoice()
                .otherwise()
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(401))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .setBody(constant(null))
                .endChoice()
            .end();

        from(platformHttp("/authenticate")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");
            
        from("direct:validateAutentication")
            .log(DEBUG, ">>> validateAutentication")
            .choice()
                .when(simple("${headers.CamelHttpMethod} != 'OPTIONS'"))
                    .process(e -> {
                        final String requestTokenHeader = e.getIn().getHeader("Authorization", String.class);
                        System.out.println("requestTokenHeader:" + requestTokenHeader);
                        System.out.println("Auth Enabled");
                        String username = null;
                        String jwtToken = null;
                        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                            jwtToken = requestTokenHeader.substring(7);
                            try {
                                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                            } catch (IllegalArgumentException e2) {
                                System.out.println("Unable to get JWT Token");
                            } catch (ExpiredJwtException e2) {
                                System.out.println("JWT Token has expired");
                            }
                        }else{
                            e.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
                            e.getIn().setBody(null);
                            return;
                        }
                        if (username != null){
                            userTransaction.begin();
                            TypedQuery<User> userQuery = entityManager.createQuery("select u from User u where u.name = :name", User.class);
                            userQuery.setParameter("name", username);
                            List<User> userList = userQuery.getResultList();
                            if (userList.size() == 0){
                                System.out.println("userList.size():" + userList.size());
                                e.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
                                e.getIn().setBody(null);
                                return;
                            }
                            User user = userList.get(0);
                            if (jwtTokenUtil.validateToken(jwtToken, user)) {
                                System.out.println("jwtToken Validated:" + jwtToken);
                                //POCUser aUser = new POCUser(user.getId(), user.getName(), user.getPassword(), null);
                                e.setProperty("user", user);
                            }
                            userTransaction.commit();
                            e.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 201);
                        }
                    })
                    .log(DEBUG, ">>> xxx outer")
                    .choice()
                        .when(simple("${headers.CamelHttpResponseCode} == 401"))
                            .log(DEBUG, ">>> xxx")
                            .stop()
                        .endChoice()
                    .end()
                //.endChoice()
            .end();
    }
}