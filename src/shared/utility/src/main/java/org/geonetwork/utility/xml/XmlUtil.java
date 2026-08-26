/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
/*
 * (c) 2003 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license,
 * available at the root application directory.
 */
package org.geonetwork.utility.xml;

import java.io.StringReader;
import java.util.Map;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class XmlUtil {

    public XdmValue getByXpath(String xml, String xpath, Map<String, String> namespacePrefixAndUri)
            throws SaxonApiException {
        Processor proc = new Processor(false);
        XPathCompiler xpathCompiler = proc.newXPathCompiler();
        XdmNode xmlDocument = proc.newDocumentBuilder().build(new StreamSource(new StringReader(xml)));
        namespacePrefixAndUri.forEach(xpathCompiler::declareNamespace);
        XPathExecutable exe = xpathCompiler.compile(xpath);
        XPathSelector selector = exe.load();
        selector.setContextItem(xmlDocument);
        return selector.evaluate();
    }
}
