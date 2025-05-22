package br.com.lkm.taxone.mapper.route;

import static org.apache.camel.LoggingLevel.DEBUG;

import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import br.com.lkm.taxone.mapper.dto.EmailDTO;
import br.com.lkm.taxone.mapper.entity.Email;
import jakarta.enterprise.context.ApplicationScoped;


/**
 * Camel route definitions.
 */
@ApplicationScoped
public class EmailRoutes extends EndpointRouteBuilder {
	
    @Override
    public void configure() throws Exception {

		interceptFrom()
			.to("direct:validateAutentication")
			.to("direct:cors");
		
		from(platformHttp("/emails")
			.httpMethodRestrict("GET"))
			.process(e -> {
				System.out.println("in emails");
				e.getIn().setHeader("page", e.getIn().getHeader("page", Integer.class) * e.getIn().getHeader("size", Integer.class));
				e.getIn().setHeader("CamelJpaMaximumResults", e.getIn().getHeader("size", Integer.class));
			})
			.toD("jpa:?query=select count(e) from Email e")
			.setProperty("count", simple("${body.size}", Integer.class))
			.setBody(constant((Object)null))
			.toD("jpa:?query=select e from Email e&firstResult=${headers.page}")
			.convertBodyTo(EmailDTO[].class)
			.to("direct:createPage")
			.setBody(simple("${exchangeProperty[pageResponse]}"))
			.marshal().json()
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
			.log(DEBUG, ">>> ${body}");

		from(platformHttp("/emails")
			.httpMethodRestrict("POST"))
			.convertBodyTo(String.class)
			.unmarshal().json(Email[].class)
			.split(body())
				.to("jpa:Email?useExecuteUpdate=true")
			.end()
			.marshal().json()
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
			.log(DEBUG, ">>> dsTables:${body}");

		from(platformHttp("/emails")
			.httpMethodRestrict("OPTIONS"))
			.setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
			.log(DEBUG, ">>> in Options");

		from(platformHttp("/emails/{id}")
			.httpMethodRestrict("DELETE"))
			.toD("jpa:?query=delete from Email e where e.id = ${headers.id}&useExecuteUpdate=true")
			.setBody(constant((Object)null))
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
			.log(DEBUG, ">>> dsTables:${body}");

		from(platformHttp("/emails/{id}")
			.httpMethodRestrict("OPTIONS"))
			.setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
			.log(DEBUG, ">>> in Options");

	}
}