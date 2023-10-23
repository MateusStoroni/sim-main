package io.sim;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Itinerary {

	private boolean on;
	private String uriItineraryXML;
	private String[] itinerary;
	private String idItinerary;

	public Itinerary(String _uriRoutesXML, String _idRoute) {
		this.uriItineraryXML = _uriRoutesXML;
		this.idItinerary = _idRoute;
		this.uriItineraryXML = _uriRoutesXML;
		this.idItinerary = _idRoute;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(this.uriItineraryXML);
			NodeList nList = doc.getElementsByTagName("vehicle");

			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element elem = (Element) nNode;
					String idRouteAux = elem.getAttribute("id"); // Obtendo o ID do veículo

					if (idRouteAux.equals(this.idItinerary)) { // Verificando se o ID do veículo corresponde ao ID
																// desejado
						Node node = elem.getElementsByTagName("route").item(0);
						Element edges = (Element) node;
						this.itinerary = new String[] { idRouteAux, edges.getAttribute("edges") };
						break; // Saindo do loop assim que a rota desejada for encontrada
					}
				}
			}

			if (this.itinerary != null) {
				System.out.println(itinerary[0] + "; " + itinerary[1]);
				this.on = true;
			} else {
				System.out.println("Rota com ID " + this.idItinerary + " não encontrada.");
				this.on = false;
			}

		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			this.on = false;
		}
	}

	public String getIDItinerary() {
		return this.idItinerary;
	}

	public String getUriItineraryXML() {
		return this.uriItineraryXML;
	}

	public String[] getItinerary() {
		return this.itinerary;
	}

	public String getIdItinerary() {
		return this.idItinerary;
	}

	public boolean isOn() {
		return this.on;
	}
}