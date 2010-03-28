package odata4j.producer.resources;

import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import odata4j.core.ODataConstants;
import odata4j.core.OProperty;
import odata4j.expression.BoolCommonExpression;
import odata4j.expression.CommonExpression;
import odata4j.expression.ExpressionParser;
import odata4j.expression.OrderByExpression;
import odata4j.producer.EntitiesResponse;
import odata4j.producer.EntityResponse;
import odata4j.producer.ODataProducer;
import odata4j.producer.QueryInfo;
import odata4j.xml.AtomFeedWriter;

import com.sun.jersey.api.core.HttpContext;

@Path("{entitySetName}")
public class EntitiesRequestResource extends BaseResource {

	private static final Logger log = Logger.getLogger(EntitiesRequestResource.class.getName());
	

	@POST
	@Produces(ODataConstants.APPLICATION_ATOM_XML_CHARSET)
	public Response createEntity(
			@Context HttpContext context,
			@Context ODataProducer producer,
			final @PathParam("entitySetName") String entitySetName){
		
		log.info(String.format("createEntity(%s)",entitySetName));
		
		List<OProperty<?>> properties = this.getRequestEntityProperties(context.getRequest());

		EntityResponse response = producer.createEntity(entitySetName,properties);
		
		String baseUri = context.getUriInfo().getBaseUri().toString();
		StringWriter sw = new StringWriter();
		String entryId = AtomFeedWriter.generateResponseEntry(baseUri,response,sw);
		String responseEntity = sw.toString();
		
		return Response.ok(responseEntity,ODataConstants.APPLICATION_ATOM_XML_CHARSET)
					.status(Status.CREATED)
					.location(URI.create(entryId))
					.header("DataServiceVersion","1.0").build();
		
	}
	
	
	@GET
	@Produces(ODataConstants.APPLICATION_ATOM_XML_CHARSET)
	public Response getEntities(
			@Context HttpContext context,
			@Context ODataProducer producer,
		    @PathParam("entitySetName") String entitySetName, 
			@QueryParam("$top") String top, 
			@QueryParam("$skip") String skip,
			@QueryParam("$filter") String filter,
			@QueryParam("$orderby") String orderBy){
		
		log.info(String.format("getEntities(%s,%s,%s,%s,%s)",entitySetName,top,skip,filter,orderBy));
		
		final QueryInfo finalQuery = new QueryInfo(parseTop(top),parseSkip(skip),parseFilter(filter),parseOrderBy(orderBy));
		
		
		EntitiesResponse response = producer.getEntities(entitySetName,finalQuery);
		
		String baseUri = context.getUriInfo().getBaseUri().toString();
		StringWriter sw = new StringWriter();
		AtomFeedWriter.generateFeed(baseUri,response,sw);
		String entity = sw.toString();
		//log.info("entity: " + entity);
		return Response.ok(entity,ODataConstants.APPLICATION_ATOM_XML_CHARSET).header("DataServiceVersion","1.0").build();
		
	}
	
	private Integer parseTop(String top){
		return top==null?null:Integer.parseInt(top);
	}
	private Integer parseSkip(String skip){
		return skip==null?null:Integer.parseInt(skip);
	}
	private BoolCommonExpression parseFilter(String filter){
		if (filter==null)
			return null;
		CommonExpression ce = ExpressionParser.parse(filter);
		if (!(ce instanceof BoolCommonExpression))
			throw new RuntimeException("Bad filter");
		return (BoolCommonExpression)ce;
	}
	private List<OrderByExpression> parseOrderBy(String orderBy){
		if (orderBy==null)
			return null;
		return ExpressionParser.parseOrderBy(orderBy);
	}

}