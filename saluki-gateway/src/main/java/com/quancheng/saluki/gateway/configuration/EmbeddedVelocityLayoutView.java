/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.configuration;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.boot.web.servlet.view.velocity.EmbeddedVelocityToolboxView;
import org.springframework.core.NestedIOException;

import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.Locale;

/**
 * @author shimingliu 2017年2月13日 下午3:25:40
 * @version EmbeddedVelocityLayoutView.java, v 0.0.1 2017年2月13日 下午3:25:40 shimingliu
 */
@SuppressWarnings("deprecation")
public class EmbeddedVelocityLayoutView extends EmbeddedVelocityToolboxView {

    /**
     * The default {@link #setLayoutUrl(String) layout url}.
     */
    public static final String DEFAULT_LAYOUT_URL         = "layout.vm";

    /**
     * The default {@link #setLayoutKey(String) layout key}.
     */
    public static final String DEFAULT_LAYOUT_KEY         = "layout";

    /**
     * The default {@link #setScreenContentKey(String) screen content key}.
     */
    public static final String DEFAULT_SCREEN_CONTENT_KEY = "screen_content";

    private String             layoutUrl                  = DEFAULT_LAYOUT_URL;

    private String             layoutKey                  = DEFAULT_LAYOUT_KEY;

    private String             screenContentKey           = DEFAULT_SCREEN_CONTENT_KEY;

    /**
     * Set the layout template to use. Default is {@link #DEFAULT_LAYOUT_URL "layout.vm"}.
     * 
     * @param layoutUrl the template location (relative to the template root directory)
     */
    public void setLayoutUrl(String layoutUrl) {
        this.layoutUrl = layoutUrl;
    }

    /**
     * Set the context key used to specify an alternate layout to be used instead of the default layout. Screen content
     * templates can override the layout template that they wish to be wrapped with by setting this value in the
     * template, for example:<br>
     * {@code #set($layout = "MyLayout.vm" )}
     * <p>
     * Default key is {@link #DEFAULT_LAYOUT_KEY "layout"}, as illustrated above.
     * 
     * @param layoutKey the name of the key you wish to use in your screen content templates to override the layout
     * template
     */
    public void setLayoutKey(String layoutKey) {
        this.layoutKey = layoutKey;
    }

    /**
     * Set the name of the context key that will hold the content of the screen within the layout template. This key
     * must be present in the layout template for the current screen to be rendered.
     * <p>
     * Default is {@link #DEFAULT_SCREEN_CONTENT_KEY "screen_content"}: accessed in VTL as {@code $screen_content}.
     * 
     * @param screenContentKey the name of the screen content key to use
     */
    public void setScreenContentKey(String screenContentKey) {
        this.screenContentKey = screenContentKey;
    }

    /**
     * Overrides {@code VelocityView.checkTemplate()} to additionally check that both the layout template and the screen
     * content template can be loaded. Note that during rendering of the screen content, the layout template can be
     * changed which may invalidate any early checking done here.
     */
    @Override
    public boolean checkResource(Locale locale) throws Exception {
        if (!super.checkResource(locale)) {
            return false;
        }

        try {
            // Check that we can get the template, even if we might subsequently get it again.
            getTemplate(this.layoutUrl);
            return true;
        } catch (ResourceNotFoundException ex) {
            throw new NestedIOException("Cannot find Velocity template for URL [" + this.layoutUrl
                                        + "]: Did you specify the correct resource loader path?", ex);
        } catch (Exception ex) {
            throw new NestedIOException("Could not load Velocity template for URL [" + this.layoutUrl + "]", ex);
        }
    }

    /**
     * Overrides the normal rendering process in order to pre-process the Context, merging it with the screen template
     * into a single value (identified by the value of screenContentKey). The layout template is then merged with the
     * modified Context in the super class.
     */
    @Override
    protected void doRender(Context context, HttpServletResponse response) throws Exception {
        renderScreenContent(context);

        // Velocity context now includes any mappings that were defined
        // (via #set) in screen content template.
        // The screen template can overrule the layout by doing
        // #set( $layout = "MyLayout.vm" )
        String layoutUrlToUse = (String) context.get(this.layoutKey);
        if (layoutUrlToUse != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Screen content template has requested layout [" + layoutUrlToUse + "]");
            }
        } else {
            // No explicit layout URL given -> use default layout of this view.
            layoutUrlToUse = this.layoutUrl;
        }

        mergeTemplate(getTemplate(layoutUrlToUse), context, response);
    }

    /**
     * The resulting context contains any mappings from render, plus screen content.
     */
    private void renderScreenContent(Context velocityContext) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Rendering screen content template [" + getUrl() + "]");
        }

        StringWriter sw = new StringWriter();
        Template screenContentTemplate = getTemplate(getUrl());
        screenContentTemplate.merge(velocityContext, sw);

        // Put rendered content into Velocity context.
        velocityContext.put(this.screenContentKey, sw.toString());
    }
}
