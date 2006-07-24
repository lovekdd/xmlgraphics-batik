/*

   Copyright 2000-2001,2003,2006  The Apache Software Foundation 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.dom.svg;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.batik.parser.ParseException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedTransformList;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGTransform;
import org.w3c.dom.svg.SVGTransformList;

/**
 * This class is the implementation of the SVGAnimatedTransformList interface.
 *
 * @author <a href="mailto:nicolas.socheleau@bitflash.com">Nicolas Socheleau</a>
 * @version $Id$
 */
public class SVGOMAnimatedTransformList 
        extends AbstractSVGAnimatedValue
        implements SVGAnimatedTransformList {

    /**
     * The base value.
     */
    protected BaseSVGTransformList baseVal;

    /**
     * The animated value.
     */
    protected AnimSVGTransformList animVal;

    /**
     * Whether the list is changing.
     */
    protected boolean changing;

    /**
     * Default value for the 'transform' attribute.
     */
    protected String defaultValue;

    /**
     * Creates a new SVGOMAnimatedTransformList.
     * @param elt The associated element.
     * @param ns The attribute's namespace URI.
     * @param ln The attribute's local name.
     * @param defaultValue The default value if the attribute is not specified.
     */
    public SVGOMAnimatedTransformList(AbstractElement elt,
                                      String ns,
                                      String ln,
                                      String defaultValue) {
        super(elt, ns, ln);
        this.defaultValue = defaultValue;
    }

    /**
     * <b>DOM</b>: Implements {@link SVGAnimatedTransformList#getBaseVal()}.
     */
    public SVGTransformList getBaseVal() {
        if (baseVal == null) {
            baseVal = new BaseSVGTransformList();
        }
        return baseVal;
    }

    /**
     * <b>DOM</b>: Implements {@link SVGAnimatedTransformList#getAnimVal()}.
     */
    public SVGTransformList getAnimVal() {
        if (animVal == null) {
            animVal = new AnimSVGTransformList();
        }
        return animVal;
    }

    /**
     * Sets the animated value to a single transform.
     */
    public void setAnimatedValue(SVGTransform t) {
        if (animVal == null) {
            animVal = new AnimSVGTransformList();
        }
        hasAnimVal = true;
        animVal.setAnimatedValue(t);
        fireAnimatedAttributeListeners();
    }

    /**
     * Sets the animated value to a list of transforms.
     * @param i an {@link Iterator} of {@link SVGTransform} objects.
     */
    public void setAnimatedValue(Iterator i) {
        if (animVal == null) {
            animVal = new AnimSVGTransformList();
        }
        hasAnimVal = true;
        animVal.setAnimatedValue(i);
        fireAnimatedAttributeListeners();
    }

    /**
     * Resets the animated value.
     */
    public void resetAnimatedValue() {
        hasAnimVal = false;
        fireAnimatedAttributeListeners();
    }

    /**
     * Called when an Attr node has been added.
     */
    public void attrAdded(Attr node, String newv) {
        if (!changing && baseVal != null) {
            baseVal.invalidate();
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }

    /**
     * Called when an Attr node has been modified.
     */
    public void attrModified(Attr node, String oldv, String newv) {
        if (!changing && baseVal != null) {
            baseVal.invalidate();
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }

    /**
     * Called when an Attr node has been removed.
     */
    public void attrRemoved(Attr node, String oldv) {
        if (!changing && baseVal != null) {
            baseVal.invalidate();
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }

    /**
     * {@link SVGTransformList} implementation for the base transform list value.
     */
    public class BaseSVGTransformList extends AbstractSVGTransformList {

        /**
         * Create a DOMException.
         */
        protected DOMException createDOMException(short type, String key,
                                                  Object[] args) {
            return element.createDOMException(type, key, args);
        }

        /**
         * Create a SVGException.
         */
        protected SVGException createSVGException(short type, String key,
                                                  Object[] args) {

            return ((SVGOMElement)element).createSVGException(type, key, args);
        }

        /**
         * Returns the value of the DOM attribute containing the transform list.
         */
        protected String getValueAsString() {
            Attr attr = element.getAttributeNodeNS(namespaceURI, localName);
            if (attr == null) {
                return defaultValue;
            }
            return attr.getValue();
        }

        /**
         * Sets the DOM attribute value containing the transform list.
         */
        protected void setAttributeValue(String value) {
            try {
                changing = true;
                element.setAttributeNS(namespaceURI, localName, value);
            } finally {
                changing = false;
            }
        }

        /**
         * Initializes the list, if needed.
         */
        protected void revalidate() {
            if (valid) {
                return;
            }

            String s = getValueAsString();
            if (s == null) {
                throw new LiveAttributeException(element, localName, true,
                                                 null);
            }
            try {
                ListBuilder builder = new ListBuilder();

                doParse(s, builder);

                if (builder.getList() != null) {
                    clear(itemList);
                }
                itemList = builder.getList();
            } catch (ParseException e) {
                itemList = new ArrayList(1);
                valid = true;
                throw new LiveAttributeException(element, localName, false, s);
            }
            valid = true;
        }
    }

    /**
     * {@link SVGTransformList} implementation for the animated transform list
     * value.
     */
    protected class AnimSVGTransformList extends AbstractSVGTransformList {

        /**
         * Creates a new AnimSVGTransformList.
         */
        public AnimSVGTransformList() {
            itemList = new ArrayList(1);
        }

        /**
         * Create a DOMException.
         */
        protected DOMException createDOMException(short type, String key,
                                                  Object[] args) {
            return element.createDOMException(type, key, args);
        }

        /**
         * Create a SVGException.
         */
        protected SVGException createSVGException(short type, String key,
                                                  Object[] args) {

            return ((SVGOMElement)element).createSVGException(type, key, args);
        }

        /**
         * <b>DOM</b>: Implements {@link SVGTransformList#getNumberOfItems()}.
         */
        public int getNumberOfItems() {
            if (hasAnimVal) {
                return super.getNumberOfItems();
            }
            return getBaseVal().getNumberOfItems();
        }

        /**
         * <b>DOM</b>: Implements {@link SVGTransformList#getItem(int)}.
         */
        public SVGTransform getItem(int index) throws DOMException {
            if (hasAnimVal) {
                return super.getItem(index);
            }
            return getBaseVal().getItem(index);
        }

        /**
         * Returns the value of the DOM attribute containing the transform list.
         */
        protected String getValueAsString() {
            if (itemList.size() == 0) {
                return "";
            }
            StringBuffer sb = new StringBuffer();
            Iterator i = itemList.iterator();
            if (i.hasNext()) {
                sb.append(((SVGItem) i.next()).getValueAsString());
            }
            while (i.hasNext()) {
                sb.append(getItemSeparator());
                sb.append(((SVGItem) i.next()).getValueAsString());
            }
            return sb.toString();
        }

        /**
         * Sets the DOM attribute value containing the transform list.
         */
        protected void setAttributeValue(String value) {
        }

        /**
         * <b>DOM</b>: Implements {@link SVGTransformList#clear()}.
         */
        public void clear() throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.transform.list", null);
        }

        /**
         * <b>DOM</b>: Implements {@link SVGTransformList#initialize(SVGTransform)}.
         */
        public SVGTransform initialize(SVGTransform newItem)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.transform.list", null);
        }

        /**
         * <b>DOM</b>: Implements {@link
         * SVGTransformList#insertItemBefore(SVGTransform, int)}.
         */
        public SVGTransform insertItemBefore(SVGTransform newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.transform.list", null);
        }

        /**
         * <b>DOM</b>: Implements {@link
         * SVGTransformList#replaceItem(SVGTransform, int)}.
         */
        public SVGTransform replaceItem(SVGTransform newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.transform.list", null);
        }

        /**
         * <b>DOM</b>: Implements {@link SVGTransformList#removeItem(int)}.
         */
        public SVGTransform removeItem(int index) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.transform.list", null);
        }

        /**
         * <b>DOM</b>: Implements {@link SVGTransformList#appendItem(SVGTransform)}.
         */
        public SVGTransform appendItem(SVGTransform newItem) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.transform.list", null);
        }

        /**
         * <b>DOM</b>: Implements {@link SVGTransformList#consolidate()}.
         */
        public SVGTransform consolidate() {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.transform.list", null);
        }

        /**
         * Sets the animated value to a list of transforms.
         */
        protected void setAnimatedValue(Iterator it) {
            int size = itemList.size();
            int i = 0;
            while (i < size && it.hasNext()) {
                SVGTransformItem t = (SVGTransformItem) itemList.get(i);
                t.assign((SVGTransform) it.next());
                i++;
            }
            while (it.hasNext()) {
                appendItemImpl(new SVGTransformItem((SVGTransform) it.next()));
                i++;
            }
            while (size > i) {
                removeItemImpl(--size);
            }
        }

        /**
         * Sets the animated value to a single transform.
         */
        protected void setAnimatedValue(SVGTransform transform) {
            int size = itemList.size();
            while (size > 1) {
                removeItemImpl(--size);
            }
            if (size == 0) {
                appendItemImpl(new SVGTransformItem(transform));
            } else {
                SVGTransformItem t = (SVGTransformItem) itemList.get(0);
                t.assign(transform);
            }
        }

        /**
         * Resets the value of the associated attribute.  Does nothing, since
         * there is no attribute for an animated value.
         */
        protected void resetAttribute() {
        }

        /**
         * Resets the value of the associated attribute.  Does nothing, since
         * there is no attribute for an animated value.
         */
        protected void resetAttribute(SVGItem item) {
        }

        /**
         * Initializes the list, if needed.  Does nothing, since there is no
         * attribute to read the list from.
         */
        protected void revalidate() {
            valid = true;
        }
    }
}
