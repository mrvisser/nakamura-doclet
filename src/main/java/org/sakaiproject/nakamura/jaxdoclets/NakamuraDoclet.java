/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.jaxdoclets;

import com.lunatech.doclets.jax.JAXDoclet;
import com.lunatech.doclets.jax.jaxb.JAXBDoclet;
import com.lunatech.doclets.jax.jaxrs.JAXRSDoclet;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.formats.html.HtmlDoclet;
import com.sun.tools.doclets.internal.toolkit.AbstractDoclet;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
@SuppressWarnings("restriction")
public class NakamuraDoclet extends JAXDoclet<NakamuraJAXConfiguration> {
  
  public static int optionLength(final String option) {
    return HtmlDoclet.optionLength(option);
  }

  public static boolean validOptions(final String[][] options, final DocErrorReporter reporter) {
    return HtmlDoclet.validOptions(options, reporter);
  }

  public static LanguageVersion languageVersion() {
    return AbstractDoclet.languageVersion();
  }

  public NakamuraDoclet(RootDoc rootDoc) {
    super(rootDoc);
  }

  @Override
  protected NakamuraJAXConfiguration makeConfiguration(ConfigurationImpl configuration) {
    return new NakamuraJAXConfiguration(configuration);
  }

  public static boolean start(final RootDoc rootDoc) {
    new NakamuraDoclet(rootDoc).start();
    return true;
  }

  public void start() {
    // first run standard docs
    HtmlDoclet.start(conf.parentConfiguration.root);
    
    // now run jax-docs.
    String baseDir = conf.parentConfiguration.destDirName;
    
    // first generate jaxb docs
    JAXBDoclet jaxb = new JAXBDoclet(conf.parentConfiguration.root);
    addOrReplaceOption(jaxb.conf.parentConfiguration, "-d", baseDir+"jaxb/");
    invokeQuiet(jaxb, "start");
    
    // generate jaxrs docs, with links back to jaxb docs
    JAXRSDoclet jaxrs = new JAXRSDoclet(conf.parentConfiguration.root);
    addOption(jaxrs.conf.parentConfiguration, "-link", "../jaxb");
    addOrReplaceOption(jaxrs.conf.parentConfiguration, "-d", baseDir+"jaxrs/");
    jaxrs.start();
  }

  /**
   * Add the option keyed by {@code key} to the list of available options. This will
   * also reset the RootDoc with the new set of options.
   * 
   * @param parentConfiguration
   * @param key
   * @param value
   */
  private void addOption(ConfigurationImpl parentConfiguration, String key, String value) {
    List<String[]> options = new ArrayList<String[]>(Arrays.asList(
        parentConfiguration.root.options()));
    options.add(new String[] {key, value});
  
    // reset the RootDoc by injecting the new set of options and re-invoking setOPtions()
    setQuiet(parentConfiguration.root, "options", com.sun.tools.javac.util.List.from(
        options.toArray(new String[0][0])));
    parentConfiguration.setOptions();
  }
  
  /**
   * Add and replace (if exists) the option keyed by {@code key} to the list of available options. This will
   * also reset the RootDoc with the new set of options.
   * 
   * @param parentConfiguration
   * @param key
   * @param value
   */
  private void addOrReplaceOption(ConfigurationImpl parentConfiguration, String key, String value) {
    List<String[]> options = new ArrayList<String[]>(Arrays.asList(
        parentConfiguration.root.options()));
    Iterator<String[]> i = options.iterator();
    while (i.hasNext()) {
      String[] option = i.next();
      if (key.equals(option[0])) {
        i.remove();
      }
    }
    
    options.add(new String[] {key, value});
    
    // reset the RootDoc by injecting the new set of options and re-invoking setOPtions()
    setQuiet(parentConfiguration.root, "options", com.sun.tools.javac.util.List.from(
        options.toArray(new String[0][0])));
    parentConfiguration.setOptions();
  }

  /**
   * Reflection utility to set a (possibly private) field value.
   * 
   * @param object
   * @param fieldName
   * @param value
   */
  private void setQuiet(Object object, String fieldName, Object value) {
    Field[] fs = object.getClass().getDeclaredFields();
    for (Field f : fs) {
      if (fieldName.equals(f.getName())) {
        f.setAccessible(true);
        try {
          f.set(object, value);
        } catch (IllegalArgumentException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }
  
  /**
   * Reflection utility to invoke a (possibly private) method.
   * 
   * @param object
   * @param methodName
   */
  private void invokeQuiet(Object object, String methodName) {
    try {
      Method ms[] = object.getClass().getDeclaredMethods();
      for (Method m : ms) {
        if (methodName.equals(m.getName()) && m.getParameterTypes().length == 0) {
          m.setAccessible(true);
          m.invoke(object);
        }
      }
    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
}
