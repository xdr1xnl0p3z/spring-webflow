package org.springframework.webflow.mvc.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.tiles.Attribute;
import org.apache.tiles.AttributeContext;
import org.apache.tiles.Definition;
import org.apache.tiles.access.TilesAccess;
import org.apache.tiles.impl.BasicTilesContainer;
import org.apache.tiles.preparer.ViewPreparer;
import org.apache.tiles.request.ApplicationContext;
import org.apache.tiles.request.Request;
import org.apache.tiles.request.servlet.ServletRequest;
import org.apache.tiles.request.servlet.wildcard.WildcardServletApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.context.servlet.DefaultAjaxHandler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;

public class AjaxTiles3ViewTests {

	private AjaxTiles3View ajaxTilesView;

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	private MockServletContext servletContext;


	@BeforeEach
	public void setUp() throws Exception {

		servletContext = new MockServletContext("/org/springframework/webflow/mvc/view/");
		request = new MockHttpServletRequest(servletContext);
		response = new MockHttpServletResponse();

		TilesConfigurer tc = new TilesConfigurer();
		tc.setDefinitions("tiles-definitions.xml");
		tc.setValidateDefinitions(true);
		tc.setServletContext(servletContext);
		tc.setUseMutableTilesContainer(false);
		tc.afterPropertiesSet();

		ajaxTilesView = new AjaxTiles3View();
	}

	private void setupStaticWebApplicationContext() {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(servletContext);
		wac.refresh();
		request.setAttribute(RequestContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		ajaxTilesView.setApplicationContext(wac);
	}

	@Test
	public void testFullPageRendering() throws Exception {
		setupStaticWebApplicationContext();
		ajaxTilesView.setUrl("search");
		ajaxTilesView.afterPropertiesSet();
		ajaxTilesView.renderMergedOutputModel(new HashMap<>(), request, response);
		assertEquals("/WEB-INF/layout.jsp", response.getForwardedUrl());
	}

	@Test
	public void testAjaxRequestNoFragments() throws Exception {
		setupStaticWebApplicationContext();
		request.addHeader("Accept", DefaultAjaxHandler.AJAX_ACCEPT_CONTENT_TYPE);
		ajaxTilesView.setUrl("search");
		ajaxTilesView.afterPropertiesSet();
		ajaxTilesView.renderMergedOutputModel(new HashMap<>(), request, response);
		assertEquals("/WEB-INF/layout.jsp", response.getForwardedUrl());
	}

	@Test
	public void testRenderFragment_Template() throws Exception {
		setupStaticWebApplicationContext();
		request.addHeader("Accept", DefaultAjaxHandler.AJAX_ACCEPT_CONTENT_TYPE);
		request.addParameter("fragments", "searchResults");
		ajaxTilesView.setUrl("search");
		ajaxTilesView.afterPropertiesSet();
		ajaxTilesView.renderMergedOutputModel(new HashMap<>(), request, response);
		assertEquals("/WEB-INF/searchResults.jsp", response.getForwardedUrl());
	}

	@Test
	public void testRenderFragment_Definition() throws Exception {
		setupStaticWebApplicationContext();
		request.addHeader("Accept", DefaultAjaxHandler.AJAX_ACCEPT_CONTENT_TYPE);
		request.addParameter("fragments", "body");
		ajaxTilesView.setUrl("search");
		ajaxTilesView.afterPropertiesSet();
		ajaxTilesView.renderMergedOutputModel(new HashMap<>(), request, response);
		assertEquals("/WEB-INF/search.jsp", response.getForwardedUrl());
	}

	@Test
	public void testRenderFragment_CascadedAttribute() throws Exception {
		setupStaticWebApplicationContext();
		request.addHeader("Accept", DefaultAjaxHandler.AJAX_ACCEPT_CONTENT_TYPE);
		request.addParameter("fragments", "searchNavigation");
		ajaxTilesView.setUrl("search");
		ajaxTilesView.afterPropertiesSet();
		ajaxTilesView.renderMergedOutputModel(new HashMap<>(), request, response);
		assertEquals("/WEB-INF/searchNavigation.jsp", response.getForwardedUrl());
	}

	@Test
	public void testRenderFragment_InheritCascadedAttribute() throws Exception {
		ApplicationContext tilesAppContext = new WildcardServletApplicationContext(servletContext);
		Request tilesRequest = new ServletRequest(tilesAppContext, request, response);
		BasicTilesContainer container = (BasicTilesContainer) TilesAccess.getContainer(tilesAppContext);
		Definition definition = container.getDefinitionsFactory().getDefinition("search.body", tilesRequest);
		definition.setPreparer(AttributeTestingPreparer.class.getName());
		setupStaticWebApplicationContext();
		request.addHeader("Accept", DefaultAjaxHandler.AJAX_ACCEPT_CONTENT_TYPE);
		request.addParameter("fragments", "body");
		ajaxTilesView.setUrl("search");
		ajaxTilesView.afterPropertiesSet();
		ajaxTilesView.renderMergedOutputModel(new HashMap<>(), request, response);
		assertTrue(AttributeTestingPreparer.invoked);
	}

	@Test
	public void testRenderFragment_DynamicAttribute() throws Exception {
		ApplicationContext tilesAppContext = new WildcardServletApplicationContext(servletContext);
		Request tilesRequest = new ServletRequest(tilesAppContext, request, response);
		BasicTilesContainer container = (BasicTilesContainer) TilesAccess.getContainer(tilesAppContext);
		AttributeContext attributeContext = container.startContext(tilesRequest);
		attributeContext.putAttribute("body", new Attribute("/WEB-INF/dynamicTemplate.jsp"));
		Map<String, Attribute> resultMap = new HashMap<>();
		ajaxTilesView.addRuntimeAttributes(container, tilesRequest, resultMap);
		assertNotNull(resultMap.get("body"));
		assertEquals("/WEB-INF/dynamicTemplate.jsp", resultMap.get("body").toString());
		container.endContext(tilesRequest);
	}

	@Test
	public void testRenderFragment_Multiple() throws Exception {
		setupStaticWebApplicationContext();
		request.addHeader("Accept", DefaultAjaxHandler.AJAX_ACCEPT_CONTENT_TYPE);
		request.addParameter("fragments", "body,searchNavigation");
		ajaxTilesView.setUrl("search");
		ajaxTilesView.afterPropertiesSet();
		ajaxTilesView.renderMergedOutputModel(new HashMap<>(), request, response);
		assertTrue(response.getIncludedUrls().size() == 2, "Multiple fragments should result in include, not forward");
		assertEquals("/WEB-INF/search.jsp", response.getIncludedUrls().get(0));
		assertEquals("/WEB-INF/searchNavigation.jsp", response.getIncludedUrls().get(1));
	}

	@Test
	public void testFlattenAttributeMap() throws Exception {
		ApplicationContext tilesAppContext = new WildcardServletApplicationContext(servletContext);
		Request tilesRequest = new ServletRequest(tilesAppContext, request, response);
		BasicTilesContainer container = (BasicTilesContainer) TilesAccess.getContainer(tilesAppContext);
		Definition compositeDefinition = container.getDefinitionsFactory().getDefinition("search", tilesRequest);
		Map<String, Attribute> resultMap = new HashMap<>();
		ajaxTilesView.flattenAttributeMap(container, tilesRequest, resultMap, compositeDefinition);
		assertNotNull(resultMap.get("body"));
		assertNotNull(resultMap.get("searchForm"));
		assertEquals("/WEB-INF/searchForm.jsp", resultMap.get("searchForm").toString());
		assertNotNull(resultMap.get("searchResults"));
	}

	@Test
	public void testGetRenderFragments() throws Exception {
		Map<String, Object> model = new HashMap<>();
		request.setParameter("fragments", "f1,f2,  f3");
		String[] fragments = ajaxTilesView.getRenderFragments(model, request, response);
		assertEquals("f1", fragments[0]);
		assertEquals("f2", fragments[1]);
		assertEquals("f3", fragments[2]);
	}


	public static class AttributeTestingPreparer implements ViewPreparer {

		public static boolean invoked;

		public void execute(Request tilesContext, AttributeContext attributeContext) {
			invoked = true;
			assertTrue(attributeContext.getAttribute("searchNavigation") != null);
		}

	}

}
