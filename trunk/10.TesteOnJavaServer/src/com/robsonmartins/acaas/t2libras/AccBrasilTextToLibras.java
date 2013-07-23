package com.robsonmartins.acaas.t2libras;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.robsonmartins.acaas.util.CharFilter;

public class AccBrasilTextToLibras implements TextToLibras {

	private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder docBuilder = null;
    
	private Document librasDoc = null;  
	private Document verbosDoc = null;  
	
	private static final XPathFactory xPathfactory = XPathFactory.newInstance();
	private static final XPath xpath = xPathfactory.newXPath();

    static {
    	try {
    	    docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	public AccBrasilTextToLibras(DefaultHttpClient fetcher) {
    	try {
    		InputStream istreamXml = getLibrasData("libras.xml");
    		librasDoc = docBuilder.parse(istreamXml);  
    		istreamXml = getLibrasData("verbos.xml");
    		verbosDoc = docBuilder.parse(istreamXml);  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public JSONObject convert(String text, String lang) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray items = new JSONArray();
		JSONObject tokenData = null;
		String tokens[] = CharFilter.replaceSpecial(text.toUpperCase()).split(" ");
		for (int idx = 0; idx < tokens.length; idx++) {
			String palavra = tokens[idx];
			tokenData = convertOneWord(palavra);
			if (tokenData != null) { items.put(tokenData); }
		}
		result.put("items", items);
		return result;
	}

	@Override
	public byte[] getVideo(String filename) throws Exception {
		InputStream data = getLibrasData("ogg/" + filename);
		return (data != null) ? IOUtils.toByteArray(data) : null;
	}

	private JSONObject convertOneWord(String palavra) throws Exception {
		Node node = null;
		NamedNodeMap attrs = null;
		node = getFirstNodeConjugacao(palavra);
		attrs = (node != null) ? node.getAttributes() : null;
		if (attrs != null) { 
			String verbo = attrs.getNamedItem("verbo").getNodeValue();
			if (verbo != null && !verbo.equals("")) {
				palavra = verbo;
			}
		}
		for (int suf = 0; suf < 3; suf++) {
			node = getFirstNodePalavra(palavra + ((suf != 0) ? suf : ""));
			if (node != null) { break; }
		}
		if (node == null) { return null; }
		attrs = node.getAttributes();
		if (attrs == null) { return null; }
		PalavraBean palavraBean = new PalavraBean();
		palavraBean.setClasse (attrs.getNamedItem("classe" ).getNodeValue());
		palavraBean.setDesc   (attrs.getNamedItem("desc"   ).getNodeValue());
		palavraBean.setImage  (attrs.getNamedItem("image"  ).getNodeValue());
		palavraBean.setLibras (attrs.getNamedItem("libras" ).getNodeValue());
		palavraBean.setOrigem (attrs.getNamedItem("origem" ).getNodeValue());
		palavraBean.setPalavra(attrs.getNamedItem("palavra").getNodeValue());
		palavraBean.setPort   (attrs.getNamedItem("port"   ).getNodeValue());
		palavraBean.setVideo  (attrs.getNamedItem("video"  ).getNodeValue());
		JSONObject result = new JSONObject(palavraBean);
		return result;
	}
	
	private Node getFirstNodePalavra(String palavra) throws Exception {
		StringBuilder exprStr = new StringBuilder();
		exprStr.append("/items/item[@palavra='").append(palavra.toUpperCase()).append("']");
		XPathExpression expr = xpath.compile(exprStr.toString());
		NodeList items = (NodeList) expr.evaluate(librasDoc, XPathConstants.NODESET);
		return (items != null && items.getLength() != 0) ? items.item(0) : null;
	}

	private Node getFirstNodeConjugacao(String palavra) throws Exception {
		StringBuilder exprStr = new StringBuilder();
		exprStr.append("/items/item[@conjugacao='").append(palavra.toUpperCase()).append("']");
		XPathExpression expr = xpath.compile(exprStr.toString());
		NodeList items = (NodeList) expr.evaluate(verbosDoc, XPathConstants.NODESET);
		return (items != null && items.getLength() != 0) ? items.item(0) : null;
	}

	private InputStream getLibrasData(String fileName) {
		return getClass().getResourceAsStream("/com/robsonmartins/acaas/t2libras/data/" + fileName);  
	}
}
