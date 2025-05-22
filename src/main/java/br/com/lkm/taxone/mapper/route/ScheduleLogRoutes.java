package br.com.lkm.taxone.mapper.route;

import static org.apache.camel.LoggingLevel.DEBUG;

import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import br.com.lkm.taxone.mapper.dto.ScheduleLogDTO;
import br.com.lkm.taxone.mapper.dto.ScheduleLogIntergrationErrorDTO;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ScheduleLogRoutes extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {

        interceptFrom()
            .to("direct:validateAutentication")
            .to("direct:cors");
        
        from(platformHttp("/schedulelogs")
            .httpMethodRestrict("GET"))
            .process(e -> {
                e.getIn().setHeader("page", e.getIn().getHeader("page", Integer.class) * e.getIn().getHeader("size", Integer.class));
				e.getIn().setHeader("CamelJpaMaximumResults", e.getIn().getHeader("size", Integer.class));
            })
            .choice()
                .when(header("status").isNotNull())
                    .setHeader("filter", simple(" where sl.status = '${headers.status}'"))
                .endChoice()
            .end()
            .toD("jpa:?query=select count(sl) from ScheduleLog sl ${headers.filter}")
            .setProperty("count", simple("${body.size}", Integer.class))
            .setBody(constant((Object)null))
            .toD("jpa:?query=select sl from ScheduleLog sl ${headers.filter}&firstResult=${headers.page}")
            .convertBodyTo(ScheduleLogDTO[].class)
            .to("direct:createPage")
            .setBody(simple("${exchangeProperty[pageResponse]}"))
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");
            
        from(platformHttp("/schedulelogs")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");
            
        from(platformHttp("/schedulelogs/{id}")
            .httpMethodRestrict("GET"))
            .toD("jpa:?query=select sl from ScheduleLog sl where sl.id = ${headers.id}")
            .setBody(simple("${body[0]}"))
            .convertBodyTo(ScheduleLogDTO.class)
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");

        from(platformHttp("/schedulelogs/{id}")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");
            
        from(platformHttp("/schedulelogsx/statistics")
            .httpMethodRestrict("GET"))
            .log(DEBUG, ">>> /schedulelogs/statistics")
            .toD("jpa:?query=select new br.com.lkm.taxone.mapper.dto.ScheduleLogStatisticDTO(sl.status, count(sl.id)) from ScheduleLog sl group by sl.status")
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");
            
        from(platformHttp("/schedulelogsx/statistics")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");
            
        from(platformHttp("/schedulelogs/{id}/taxOneErrors")
            .httpMethodRestrict("GET"))
            .process(e -> {
                e.getIn().setHeader("page", e.getIn().getHeader("page", Integer.class) * e.getIn().getHeader("size", Integer.class));
            })
            .toD("jpa:?query=select count(slie) from ScheduleLog slie where slie.schedule.id=${headers.id}")
            .setProperty("count", simple("${body.size}", Integer.class))
            .setBody(constant((Object)null))
            .toD("jpa:?query=select slie from ScheduleLogIntergrationError slie where slie.scheduleLog.id=${headers.id}&firstResult=${headers.page}&maximumResults=${headers.size}")
            .convertBodyTo(ScheduleLogIntergrationErrorDTO[].class)
            .to("direct:createPage")
            .setBody(simple("${exchangeProperty[pageResponse]}"))
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");
            
        from(platformHttp("/schedulelogs/{id}/taxOneErrors")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");
            
    }
    
}