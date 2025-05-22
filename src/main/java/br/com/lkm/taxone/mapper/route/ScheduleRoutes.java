package br.com.lkm.taxone.mapper.route;

import java.util.ArrayList;

import static org.apache.camel.LoggingLevel.DEBUG;

import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import br.com.lkm.taxone.mapper.dto.PeriodeDTO;
import br.com.lkm.taxone.mapper.dto.ScheduleDTO;
import br.com.lkm.taxone.mapper.entity.Schedule;
import br.com.lkm.taxone.mapper.entity.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ScheduleRoutes extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {

        interceptFrom()
            .to("direct:validateAutentication")
            .to("direct:cors");
        
        from(platformHttp("/schedules")
            .httpMethodRestrict("GET"))
            .process(e -> {
                e.getIn().setHeader("page", e.getIn().getHeader("page", Integer.class) * e.getIn().getHeader("size", Integer.class));
				e.getIn().setHeader("CamelJpaMaximumResults", e.getIn().getHeader("size", Integer.class));
            })
            .toD("jpa:?query=select count(s) from Schedule s")
            .setProperty("count", simple("${body.size}", Integer.class))
            .setBody(constant((Object)null))
            .toD("jpa:?query=select s from Schedule s&firstResult=${headers.page}")
            .convertBodyTo(ScheduleDTO[].class)
            .to("direct:createPage")
            .setBody(simple("${exchangeProperty[pageResponse]}"))
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");
            
        from(platformHttp("/schedules/{id}")
            .httpMethodRestrict("GET"))
            .toD("jpa:?query=select s from Schedule s where s.id = ${headers.id}")
            .setBody(simple("${body[0]}"))
            .convertBodyTo(ScheduleDTO.class)
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");

        from(platformHttp("/schedules/{id}/periodes")
            .httpMethodRestrict("GET"))
            .toD("jpa:?query=select s from Schedule s where s.id = ${headers.id}")
            .setBody(simple("${body[0]}"))
            .convertBodyTo(PeriodeDTO.class)
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");

        from(platformHttp("/schedules/{id}/periodes")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");


        from(platformHttp("/schedules")
            .httpMethodRestrict("POST"))
            .convertBodyTo(String.class)
            .unmarshal().json(ScheduleDTO.class)
            .convertBodyTo(Schedule.class)
            .script().simple("${body.setUser(${exchangeProperty[user]})}")
            .setProperty("schedule", simple("${body}"))
			.choice()
				.when(simple("${body.id} > 0"))
					.toD("jpa:?query=select c.id from Criteria c where c.schedule.id = ${body.id}")//
					.setProperty("criteriasList", simple("${body}"))
				.endChoice()
				.otherwise()
					.setProperty("criteriasList", constant(new ArrayList()))
				.endChoice()
			.end()
            .setBody(simple("${exchangeProperty[schedule]}"))
            .split(simple("${body.criterias}"))
                .script().simple("${body.setSchedule(${exchangeProperty[schedule]})}")
                .script().simple("${body.setId(null)}")
            .end()
            .toD("jpa:Schedule?useExecuteUpdate=true")
            .convertBodyTo(ScheduleDTO.class)
            .setProperty("schedule", simple("${body}"))
            .split(simple("${exchangeProperty[criteriasList]}"))//delete the criterias
                .setHeader("criteriaId", simple("${body}"))
                .setHeader("criteriaExcluded", constant(true))
                .split(simple("${exchangeProperty[schedule].criterias}"))
                    .choice()
                        .when(simple("${body.id} == ${headers.criteriaId}"))
                            .setHeader("criteriaExcluded", constant(false))
                        .endChoice()
                    .end()
                .end()
                .log(DEBUG, ">>> criteriaExcluded:${headers.criteriaExcluded}")
                .choice()
                    .when(simple("${headers.criteriaExcluded}"))
                        .toD("jpa:?query=delete from Criteria c where c.id = ${headers.criteriaId}")
                    .endChoice()
                .end()
            .end()
            .setBody(simple("${exchangeProperty[schedule]}"))
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");
            
        from(platformHttp("/schedules")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");

        from(platformHttp("/schedules/{id}")
            .httpMethodRestrict("DELETE"))
            .toD("jpa:?query=delete Criteria c where c.schedule.id=${headers.id}&useExecuteUpdate=true")
            .toD("jpa:?query=delete Schedule s where s.id=${headers.id}&useExecuteUpdate=true")
            .setBody(constant(null))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");
            
        from(platformHttp("/schedules/{id}")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");

    }
    
}

