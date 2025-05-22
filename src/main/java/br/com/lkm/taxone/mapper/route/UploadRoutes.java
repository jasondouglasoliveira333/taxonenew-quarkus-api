package br.com.lkm.taxone.mapper.route;

import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.LoggingLevel.WARN;

import java.util.Date;
import java.util.Map;
import java.util.List;
import java.io.InputStream;

import org.apache.camel.Exchange;
import org.apache.camel.attachment.Attachment;
import org.apache.camel.attachment.AttachmentMessage;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import br.com.lkm.taxone.mapper.dto.UploadDTO;
import br.com.lkm.taxone.mapper.util.XLSTable;
import br.com.lkm.taxone.mapper.util.JExcelHelper;
import br.com.lkm.taxone.mapper.util.IOUtil;
import br.com.lkm.taxone.mapper.entity.Upload;
import br.com.lkm.taxone.mapper.entity.User;
import br.com.lkm.taxone.mapper.enums.UploadStatus;
import br.com.lkm.taxone.mapper.service.UploadService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.activation.DataHandler;


import org.apache.camel.attachment.DefaultAttachment;

@ApplicationScoped
public class UploadRoutes extends EndpointRouteBuilder {
    
    private String HI_FOLKS = "Hi FOLKS";
	
    @Inject
    private UploadService uploadService;
    
    @Override
    public void configure() throws Exception {
        
        interceptFrom()
            .to("direct:validateAutentication")
            .to("direct:cors");
        
        from(platformHttp("/uploads")
            .httpMethodRestrict("GET"))
            .process(e -> {
                e.getIn().setHeader("page", e.getIn().getHeader("page", Integer.class) * e.getIn().getHeader("size", Integer.class));
				e.getIn().setHeader("CamelJpaMaximumResults", e.getIn().getHeader("size", Integer.class));
            })
            .toD("jpa:?query=select count(u) from Upload u")
            .setProperty("count", simple("${body.size}", Integer.class))
            .setBody(constant((Object)null))
            .toD("jpa:?query=select u from Upload u&firstResult=${headers.page}")
            .convertBodyTo(UploadDTO[].class)
            .to("direct:createPage")
            .setBody(simple("${exchangeProperty[pageResponse]}"))
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");

            
        from(platformHttp("/uploads")
            .httpMethodRestrict("POST"))
			.log(WARN, "${headers}")
            .process(e -> {
                System.out.println("e:" + e);
                AttachmentMessage attachmentMessage = e.getMessage(AttachmentMessage.class);
                Map<String, Attachment> attachmentObjects = attachmentMessage.getAttachmentObjects();
                Attachment configAttachment = attachmentObjects.get("file");
				DataHandler dh = configAttachment.getDataHandler();
				byte[] data = IOUtil.readAllBytes((InputStream)dh.getContent());
                uploadService.parseFileAndStore(dh.getName(), e.getIn().getHeader("layoutVersion", String.class), data, e.getProperty("user", User.class));
                e.getMessage().setBody("ok");
            })
            .setBody(constant("{\"name\": \"WE\"}"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .marshal().json()
            .log(DEBUG, ">>> ${body}");


        from(platformHttp("/uploads")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");

    }
}



