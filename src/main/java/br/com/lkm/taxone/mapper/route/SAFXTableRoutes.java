package br.com.lkm.taxone.mapper.route;

import static org.apache.camel.LoggingLevel.DEBUG;

import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import br.com.lkm.taxone.mapper.dto.SAFXColumnDTO;
import br.com.lkm.taxone.mapper.dto.SAFXColumnUpdateDTO;
import br.com.lkm.taxone.mapper.dto.SAFXTableDTO;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class SAFXTableRoutes extends EndpointRouteBuilder {
    
    @Override
    public void configure() throws Exception {

        interceptFrom()
            .to("direct:validateAutentication")
            .to("direct:cors");
        
        from(platformHttp("/safxTables")
            .httpMethodRestrict("GET"))
            .process(e -> {
                e.getIn().setHeader("page", e.getIn().getHeader("page", Integer.class) * e.getIn().getHeader("size", Integer.class));
				e.getIn().setHeader("CamelJpaMaximumResults", e.getIn().getHeader("size", Integer.class));
            })
            .setHeader("filter", simple(" where ((s.name like '%${headers.tableName}%' and '${headers.tableName}' <> '') or '${headers.tableName}' = '') and " + 
                " ((s.dsTableId  is not null and '${headers.justAssociated}' <> 'false') or '${headers.justAssociated}' = 'false')"))
            .log(DEBUG, ">>>${headers.filter}")
            .toD("jpa:?query=select count(s) from SAFXTable s ${headers.filter}")
            .setProperty("count", simple("${body[0]}", Integer.class))
            .setBody(constant((Object)null))
            .toD("jpa:?query=select s from SAFXTable s ${headers.filter}&firstResult=${headers.page}")
            .convertBodyTo(SAFXTableDTO[].class)
			.log(DEBUG, ">>>>SAFXTableDTO ${body.length}")
            .to("direct:createPage")
            .setBody(simple("${exchangeProperty[pageResponse]}"))
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");

        from(platformHttp("/safxTables")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");

        from(platformHttp("/safxTables/{id}")
            .httpMethodRestrict("GET"))
            .toD("jpa:?query=select s from SAFXTable s where s.id = ${headers.id}")
            .setBody(simple("${body[0]}"))
            .convertBodyTo(SAFXTableDTO.class)
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");

        from(platformHttp("/safxTables/{id}")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");

        from(platformHttp("/safxTables/{id}/safxColumns")
            .httpMethodRestrict("GET"))
            .toD("jpa:?query=select sc from SAFXColumn sc where sc.safxTable.id = ${headers.id}")
            .convertBodyTo(SAFXColumnDTO[].class)
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");

        from(platformHttp("/safxTables/{id}/safxColumns")
            .httpMethodRestrict("PUT"))
            .convertBodyTo(String.class)
            .unmarshal().json(SAFXColumnUpdateDTO[].class)
            .split(body())
				.choice()
					.when(simple("${body.dsColumnId} != null"))
						.toD("jpa:?query=update SAFXColumn sc set sc.dsColumnId = ${body.dsColumnId} where sc.id = ${body.id}&useExecuteUpdate=true")
					.endChoice()
				.end()
            .end()
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");

        from(platformHttp("/safxTables/{id}/safxColumns")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");

        from(platformHttp("/safxTables/{id}/dsTables/{dsTableId}")
            .httpMethodRestrict("PUT"))
            .toD("jpa:?query=update SAFXTable st set st.dsTableId = ${headers.dsTableId} where st.id = ${headers.id}&useExecuteUpdate=true")
            .setBody(constant(null))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");

        from(platformHttp("/safxTables/{id}/dsTables/{dsTableId}")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");
            
    }
}

