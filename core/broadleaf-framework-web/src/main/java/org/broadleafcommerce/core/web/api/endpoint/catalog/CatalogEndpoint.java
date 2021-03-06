/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadleafcommerce.core.web.api.endpoint.catalog;

import org.broadleafcommerce.cms.file.service.StaticAssetService;
import org.broadleafcommerce.common.media.domain.Media;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryAttribute;
import org.broadleafcommerce.core.catalog.domain.CategoryProductXref;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductAttribute;
import org.broadleafcommerce.core.catalog.domain.ProductBundle;
import org.broadleafcommerce.core.catalog.domain.RelatedProduct;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.catalog.domain.SkuAttribute;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.web.api.endpoint.BaseEndpoint;
import org.broadleafcommerce.core.web.api.wrapper.CategoriesWrapper;
import org.broadleafcommerce.core.web.api.wrapper.CategoryAttributeWrapper;
import org.broadleafcommerce.core.web.api.wrapper.CategoryWrapper;
import org.broadleafcommerce.core.web.api.wrapper.MediaWrapper;
import org.broadleafcommerce.core.web.api.wrapper.ProductAttributeWrapper;
import org.broadleafcommerce.core.web.api.wrapper.ProductBundleWrapper;
import org.broadleafcommerce.core.web.api.wrapper.ProductWrapper;
import org.broadleafcommerce.core.web.api.wrapper.RelatedProductWrapper;
import org.broadleafcommerce.core.web.api.wrapper.SkuAttributeWrapper;
import org.broadleafcommerce.core.web.api.wrapper.SkuWrapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class exposes catalog services as RESTful APIs.  It is dependent on
 * a JAX-RS implementation such as Jersey.  This class must be extended, with appropriate JAX-RS 
 * annotations, such as: <br></br> 
 * 
 * <code>javax.ws.rs.@Scope</code> <br></br> 
 * <code>javax.ws.rs.@Path</code> <br></br> 
 * <code>javax.ws.rs.@Produces</code> <br></br> 
 * <code>javax.ws.rs.@Consumes</code> <br></br> 
 * <code>javax.ws.rs.@Context</code> <br></br> 
 * etc... <br></br>
 * 
 * ... in the subclass.  The subclass must also be a Spring Bean.  The subclass can then override 
 * the methods, and specify custom inputs and outputs.  It will also specify 
 * <code>javax.ws.rs.@Path annotations</code>, <code>javax.ws.rs.@Context</code>, 
 * <code>javax.ws.rs.@PathParam</code>, <code>javax.ws.rs.@QueryParam</code>, 
 * <code>javax.ws.rs.@GET</code>, <code>javax.ws.rs.@POST</code>, etc...  Essentially, the subclass 
 * will override and extend the methods of this class, add new methods, and control the JAX-RS behavior 
 * using annotations according to the JAX-RS specification.
 *
 * User: Kelly Tisdell
 */
public abstract class CatalogEndpoint extends BaseEndpoint {

    @Resource(name="blCatalogService")
    protected CatalogService catalogService;

    //We don't inject this here because of a few dependency issues. Instead, we look this up dynamically
    //using the ApplicationContext
    protected StaticAssetService staticAssetService;

    /**
     * Search for {@code Product} by product id
     *
     * @param id the product id
     * @return the product instance with the given product id
     */
    public ProductWrapper findProductById(HttpServletRequest request, Long id) {
        Product product = catalogService.findProductById(id);
        if (product != null) {
            ProductWrapper wrapper;
            if (product instanceof ProductBundle) {
                wrapper = (ProductWrapper)context.getBean(ProductBundleWrapper.class.getName());
            } else {
                wrapper = (ProductWrapper)context.getBean(ProductWrapper.class.getName());
                
            }
            wrapper.wrap(product, request);
            return wrapper;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Product with Id " + id + " could not be found").build());
    }

    /**
     * Search for {@code Product} instances whose name starts with
     * or is equal to the passed in product name.
     *
     * @param name
     * @param limit the maximum number of results, defaults to 20
     * @param offset the starting point in the record set, defaults to 0
     * @return the list of product instances that fit the search criteria
     */
    public List<ProductWrapper> findProductsByName(HttpServletRequest request,
            String name,
            int limit,
            int offset) {
        List<Product> result;
        if (name == null) {
            result = catalogService.findAllProducts(limit, offset);
        } else {
            result = catalogService.findProductsByName(name, limit, offset);
        }

        List<ProductWrapper> out = new ArrayList<ProductWrapper>();
        if (result != null) {
            for (Product product : result) {
                ProductWrapper wrapper;
                if (product instanceof ProductBundle) {
                    wrapper = (ProductWrapper)context.getBean(ProductBundleWrapper.class.getName());
                } else {
                    wrapper = (ProductWrapper)context.getBean(ProductWrapper.class.getName());
                    
                }
                wrapper.wrap(product, request);
                out.add(wrapper);
            }
        }
        return out;
    }

    /**
     * Search for {@code Sku} instances for a given product
     *
     * @param id
     * @return the list of sku instances for the product
     */
    public List<SkuWrapper> findSkusByProductById(HttpServletRequest request, Long id) {
        Product product = catalogService.findProductById(id);
        if (product != null) {
            List<Sku> skus = product.getAllSkus();
            List<SkuWrapper> out = new ArrayList<SkuWrapper>();
            if (skus != null) {
                for (Sku sku : skus) {
                    SkuWrapper wrapper = (SkuWrapper)context.getBean(SkuWrapper.class.getName());
                    wrapper.wrap(sku, request);
                    out.add(wrapper);
                }
                return out;
            }
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Product with Id " + id + " could not be found").build());
    }
    
    public SkuWrapper findDefaultSkuByProductId(HttpServletRequest request, Long id) {
        Product product = catalogService.findProductById(id);
        if (product != null && product.getDefaultSku() != null) {
            SkuWrapper wrapper = (SkuWrapper)context.getBean(SkuWrapper.class.getName());
            wrapper.wrap(product.getDefaultSku(), request);
            return wrapper;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Product with Id " + id + " could not be found").build());
    }

    public CategoriesWrapper findAllCategories(HttpServletRequest request,
            String name,
            int limit,
            int offset) {
        List<Category> categories;
        if (name != null) {
            categories = catalogService.findCategoriesByName(name, limit, offset);
        } else {
            categories = catalogService.findAllCategories(limit, offset);
        }
        CategoriesWrapper wrapper = (CategoriesWrapper)context.getBean(CategoriesWrapper.class.getName());
        wrapper.wrap(categories, request);
        return wrapper;
    }

    public CategoriesWrapper findSubCategories(HttpServletRequest request,
            Long id,
            int limit,
            int offset,
            boolean active) {
        Category category = catalogService.findCategoryById(id);
        if (category != null) {
            List<Category> categories;
            CategoriesWrapper wrapper = (CategoriesWrapper)context.getBean(CategoriesWrapper.class.getName());
            if (active) {
                categories = catalogService.findActiveSubCategoriesByCategory(category, limit, offset);
            } else {
                categories = catalogService.findAllSubCategories(category, limit, offset);
            }
            wrapper.wrap(categories, request);
            return wrapper;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Category with Id " + id + " could not be found").build());

    }

    public CategoriesWrapper findActiveSubCategories(HttpServletRequest request,
            Long id,
            int limit,
            int offset) {
        Category category = catalogService.findCategoryById(id);
        if (category != null) {
            List<Category> categories = catalogService.findActiveSubCategoriesByCategory(category, limit, offset);
            CategoriesWrapper wrapper = (CategoriesWrapper)context.getBean(CategoriesWrapper.class.getName());
            wrapper.wrap(categories, request);
            return wrapper;
        }

        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Category with Id " + id + " could not be found").build());
    }

    public CategoryWrapper findCategoryById(HttpServletRequest request,
            Long id,
            int productLimit,
            int productOffset,
            int subcategoryLimit,
            int subcategoryOffset,
            int subcategoryDepth) {
        Category cat = catalogService.findCategoryById(id);
        if (cat != null) {

            //Explicitly setting these request attributes because the CategoryWrapper.wrap() method needs them
            request.setAttribute("productLimit", productLimit);
            request.setAttribute("productOffset", productOffset);
            request.setAttribute("subcategoryLimit", subcategoryLimit);
            request.setAttribute("subcategoryOffset", subcategoryOffset);
            request.setAttribute("subcategoryDepth", subcategoryDepth);

            CategoryWrapper wrapper = (CategoryWrapper)context.getBean(CategoryWrapper.class.getName());
            wrapper.wrap(cat, request);
            return wrapper;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Category with Id " + id + " could not be found").build());
    }

    public List<CategoryAttributeWrapper> findCategoryAttributesForCategory(HttpServletRequest request,
            Long id) {
        Category category = catalogService.findCategoryById(id);
        if (category != null) {
            ArrayList<CategoryAttributeWrapper> out = new ArrayList<CategoryAttributeWrapper>();
            if (category.getCategoryAttributes() != null) {
                for (CategoryAttribute attribute : category.getCategoryAttributes()) {
                    CategoryAttributeWrapper wrapper = (CategoryAttributeWrapper)context.getBean(CategoryAttributeWrapper.class.getName());
                    wrapper.wrap(attribute, request);
                    out.add(wrapper);
                }
            }
            return out;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Category with Id " + id + " could not be found").build());
    }
    
    public List<ProductWrapper> findProductsForCategory(HttpServletRequest request,
            Long id,
            int limit,
            int offset,
            boolean activeOnly) {
        Category category = catalogService.findCategoryById(id);
        if (category != null) {
            List<Product> products;
            ArrayList<ProductWrapper> out = new ArrayList<ProductWrapper>();
            if (activeOnly) {
                products = catalogService.findActiveProductsByCategory(category, new Date(), limit, offset);
            } else {
                products = catalogService.findProductsForCategory(category, limit, offset);
            }
            if (products != null) {
                for (Product product : products) {
                    ProductWrapper wrapper;
                    if (product instanceof ProductBundle) {
                        wrapper = (ProductWrapper)context.getBean(ProductBundleWrapper.class.getName());
                    } else {
                        wrapper = (ProductWrapper)context.getBean(ProductWrapper.class.getName());
                        
                    }
                    wrapper.wrap(product, request);
                    out.add(wrapper);
                }
            }
            return out;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Category with Id " + id + " could not be found").build());
    }

    public List<RelatedProductWrapper> findUpSaleProductsByProduct(HttpServletRequest request,
            Long id,
            int limit,
            int offset) {
        Product product = catalogService.findProductById(id);
        if (product != null) {
            List<RelatedProductWrapper> out = new ArrayList<RelatedProductWrapper>();

            //TODO: Write a service method that accepts offset and limit
            List<RelatedProduct> relatedProds = product.getUpSaleProducts();
            if (relatedProds != null) {
                for (RelatedProduct prod : relatedProds) {
                    RelatedProductWrapper wrapper = (RelatedProductWrapper)context.getBean(RelatedProductWrapper.class.getName());
                    wrapper.wrap(prod,request);
                    out.add(wrapper);
                }
            }
            return out;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Product with Id " + id + " could not be found").build());
    }

    public List<RelatedProductWrapper> findCrossSaleProductsByProduct(HttpServletRequest request,
            Long id,
            int limit,
            int offset) {
        Product product = catalogService.findProductById(id);
        if (product != null) {
            List<RelatedProductWrapper> out = new ArrayList<RelatedProductWrapper>();

            //TODO: Write a service method that accepts offset and limit
            List<RelatedProduct> xSellProds = product.getCrossSaleProducts();
            if (xSellProds != null) {
                for (RelatedProduct prod : xSellProds) {
                    RelatedProductWrapper wrapper = (RelatedProductWrapper)context.getBean(RelatedProductWrapper.class.getName());
                    wrapper.wrap(prod, request);
                    out.add(wrapper);
                }
            }
            return out;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Product with Id " + id + " could not be found").build());
    }
    
    public List<ProductAttributeWrapper> findProductAttributesForProduct(HttpServletRequest request,
            Long id) {
        Product product = catalogService.findProductById(id);
        if (product != null) {
            ArrayList<ProductAttributeWrapper> out = new ArrayList<ProductAttributeWrapper>();
            if (product.getProductAttributes() != null) {
                for (Map.Entry<String, ProductAttribute> entry : product.getProductAttributes().entrySet()) {
                    ProductAttributeWrapper wrapper = (ProductAttributeWrapper)context.getBean(ProductAttributeWrapper.class.getName());
                    wrapper.wrap(entry.getValue(), request);
                    out.add(wrapper);
                }
            }
            return out;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Product with Id " + id + " could not be found").build());
    }

    public List<SkuAttributeWrapper> findSkuAttributesForSku(HttpServletRequest request,
            Long id) {
        Sku sku = catalogService.findSkuById(id);
        if (sku != null) {
            ArrayList<SkuAttributeWrapper> out = new ArrayList<SkuAttributeWrapper>();
            if (sku.getSkuAttributes() != null) {
                for (Map.Entry<String, SkuAttribute> entry : sku.getSkuAttributes().entrySet()) {
                    SkuAttributeWrapper wrapper = (SkuAttributeWrapper)context.getBean(SkuAttributeWrapper.class.getName());
                    wrapper.wrap(entry.getValue(), request);
                    out.add(wrapper);
                }
            }
            return out;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Sku with Id " + id + " could not be found").build());
    }

    public List<MediaWrapper> findMediaForSku(HttpServletRequest request,
            Long id) {
        Sku sku = catalogService.findSkuById(id);
        if (sku != null) {
            List<MediaWrapper> medias = new ArrayList<MediaWrapper>();
            if (sku.getSkuMedia() != null && ! sku.getSkuMedia().isEmpty()) {
                for (Media media : sku.getSkuMedia().values()) {
                    MediaWrapper wrapper = (MediaWrapper)context.getBean(MediaWrapper.class.getName());
                    wrapper.wrap(media, request);
                    if (wrapper.isAllowOverrideUrl()){
                        wrapper.setUrl(getStaticAssetService().convertAssetPath(media.getUrl(), request.getContextPath(), request.isSecure()));
                    }
                    medias.add(wrapper);
                }
            }
            return medias;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Sku with Id " + id + " could not be found").build());
    }

    public SkuWrapper findSkuById(HttpServletRequest request,
            Long id) {
        Sku sku = catalogService.findSkuById(id);
        if (sku != null) {
            SkuWrapper wrapper = (SkuWrapper)context.getBean(SkuWrapper.class.getName());
            wrapper.wrap(sku, request);
            return wrapper;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Sku with Id " + id + " could not be found").build());
    }

    public List<MediaWrapper> findMediaForProduct(HttpServletRequest request,
            Long id) {
        Product product = catalogService.findProductById(id);
        if (product != null) {
            ArrayList<MediaWrapper> out = new ArrayList<MediaWrapper>();
            Map<String, Media> media = product.getMedia();
            if (media != null) {
                for (Media med : media.values()) {
                    MediaWrapper wrapper = (MediaWrapper)context.getBean(MediaWrapper.class.getName());
                    wrapper.wrap(med, request);
                    if (wrapper.isAllowOverrideUrl()){
                        wrapper.setUrl(getStaticAssetService().convertAssetPath(med.getUrl(), request.getContextPath(), request.isSecure()));
                    }
                    out.add(wrapper);
                }
            }
            return out;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Product with Id " + id + " could not be found").build());
    }

    public List<MediaWrapper> findMediaForCategory(HttpServletRequest request,
            Long id) {
        Category category = catalogService.findCategoryById(id);
        if (category != null) {
            ArrayList<MediaWrapper> out = new ArrayList<MediaWrapper>();
            Map<String, Media> media = category.getCategoryMedia();
            for (Media med : media.values()) {
                MediaWrapper wrapper = (MediaWrapper)context.getBean(MediaWrapper.class.getName());
                wrapper.wrap(med, request);
                out.add(wrapper);
            }
            return out;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Category with Id " + id + " could not be found").build());
    }

    public CategoriesWrapper findParentCategoriesForProduct(HttpServletRequest request,
            Long id) {
        Product product = catalogService.findProductById(id);
        if (product != null) {
            CategoriesWrapper wrapper = (CategoriesWrapper)context.getBean(CategoriesWrapper.class.getName());
            List<Category> categories = new ArrayList<Category>();
            for (CategoryProductXref categoryXref : product.getAllParentCategoryXrefs()) {
                categories.add(categoryXref.getCategory());
            }
            wrapper.wrap(categories, request);
            return wrapper;
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Product with Id " + id + " could not be found").build());
    }

    protected StaticAssetService getStaticAssetService() {
        if (staticAssetService == null) {
            staticAssetService = (StaticAssetService)this.context.getBean("blStaticAssetService");
        }
        return staticAssetService;
    }
}

