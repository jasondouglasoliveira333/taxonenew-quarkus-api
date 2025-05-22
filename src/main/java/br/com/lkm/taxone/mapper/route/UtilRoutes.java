package br.com.lkm.taxone.mapper.route;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import br.com.lkm.taxone.mapper.dto.PageResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UtilRoutes extends EndpointRouteBuilder {
    
    private String HI_FOLKS = "Hi FOLKS";
    
    @Override
    public void configure() throws Exception {

        from("direct:cors")
            .setHeader("access-control-allow-headers", constant("Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization"))
            .setHeader("access-control-allow-methods", constant("GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, CONNECT, PATCH"))
            .setHeader("access-control-allow-origin", constant("*"))
            .setHeader("access-control-max-age", constant("3600"));
            
        from("direct:createPage")
            .process(e -> {
                var pageResponse = new PageResponse();
				int count = e.getProperty("count", Integer.class);
				int size = e.getIn().getHeader("size", Integer.class);
				int totalPage = count / size;
				if (count % size != 0){
					totalPage++;
				}
                pageResponse.setTotalPages(totalPage);
                pageResponse.setContent(e.getIn().getBody(Object[].class));
                e.setProperty("pageResponse", pageResponse);
            });

            
        
    }
}
