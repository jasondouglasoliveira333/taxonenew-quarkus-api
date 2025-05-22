package br.com.lkm.taxone.mapper.route;

import static org.apache.camel.LoggingLevel.DEBUG;

import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import br.com.lkm.taxone.mapper.dto.DSColumnDTO;
import br.com.lkm.taxone.mapper.dto.DSTableDTO;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DSTableRoutes extends EndpointRouteBuilder {
    
    @Override
    public void configure() throws Exception {

        interceptFrom()
            .to("direct:validateAutentication")
            .to("direct:cors");
        
        from(platformHttp("/dsTables")
            .httpMethodRestrict("GET"))
            .toD("jpa:?query=select dt from DSTable dt")
            .convertBodyTo(DSTableDTO[].class)
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");

        from(platformHttp("/dsTables")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");

        from(platformHttp("/dsTables/{id}/dsColumns")
            .httpMethodRestrict("GET"))
            .process(e -> {
                e.getIn().setHeader("page", e.getIn().getHeader("page", Integer.class) * e.getIn().getHeader("size", Integer.class));
            })
            .toD("jpa:?query=select count(ds) from DSColumn ds where ds.dsTable.id = ${headers.id}")
            .setProperty("count", simple("${body.size}", Integer.class))
            .setBody(constant((Object)null))
            .toD("jpa:?query=select ds from DSColumn ds where ds.dsTable.id = ${headers.id}&firstResult=${headers.page}&maximumResults=${headers.size}")//
            .convertBodyTo(DSColumnDTO[].class)
            .to("direct:createPage")
            .setBody(simple("${exchangeProperty[pageResponse]}"))
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");
            
        from(platformHttp("/dsTables/{id}/dsColumns")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");

            

    }
}