<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rax="http://docs.rackspace.com/api"
    xmlns:wadl="http://wadl.dev.java.net/2009/02"
    
    version="2.0" exclude-result-prefixes="wadl">
    
    <xsl:param name="pathto">@@PATH_TO@@/</xsl:param>
    
    <xsl:template match="wadl:application">
       <html>
           <head>
               <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
           </head>
           <body>
               <h1>Wadl: <xsl:value-of select="replace(base-uri(.),'(.*/)(.*\.wadl)', '$2')"/></h1>
               <p><b>WADL Location: </b><xsl:value-of select="base-uri()"/></p>
               <ul>
                <xsl:apply-templates select="wadl:resources" mode="toc"/>
               </ul>
                <xsl:apply-templates select="wadl:resources"/>
           </body>
           <link href='http://alexgorbatchev.com/pub/sh/current/styles/shCore.css' rel='stylesheet' type='text/css' />
           <link href='http://alexgorbatchev.com/pub/sh/current/styles/shThemeDefault.css' rel='stylesheet' type='text/css' />
           <script src='http://alexgorbatchev.com/pub/sh/current/scripts/shCore.js' type='text/javascript'>&#160;</script>
           <script src='http://alexgorbatchev.com/pub/sh/current/scripts/shAutoloader.js' type='text/javascript'>&#160;</script>
           <script src='http://alexgorbatchev.com/pub/sh/current/scripts/shBrushXml.js' type='text/javascript'>&#160;</script>
           <script src='http://alexgorbatchev.com/pub/sh/current/scripts/shBrushCss.js' type='text/javascript'>&#160;</script>
           <script type="text/javascript">
               SyntaxHighlighter.config.space = '&#32;';
               SyntaxHighlighter.defaults['smart-tabs'] = false;
               SyntaxHighlighter.defaults['auto-links'] = false;
               SyntaxHighlighter.all()
  </script>
       </html>
        <xsl:if test="//wadl:resource[(./wadl:method and not(@id)) or (@type and not(@id))]">
            <xsl:message terminate="yes">
                id attribute missing on one or more wadl:resource elements. 
            </xsl:message>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="wadl:resources" mode="#all">
        <xsl:apply-templates select="wadl:resource" mode="#current"/>
    </xsl:template>
    
    <xsl:template match="wadl:resource" mode="#all">
        <xsl:param name="path">
            <xsl:for-each select="ancestor-or-self::wadl:resource/@path"><xsl:value-of select="."/><xsl:if test="not(ends-with(.,'/')) and not(position() = last())">/</xsl:if></xsl:for-each>         
        </xsl:param>
        <xsl:param name="id" select="if (@id) then @id else '??ID_MISSING??'"/>
        <xsl:param name="context" select="/"/>
        <xsl:apply-templates select="wadl:method|wadl:resource" mode="#current"/>
        <xsl:if test="not(@id)">
            <xsl:message>
                ID Missing in resource! <xsl:copy-of select="$path"/>
            </xsl:message>
        </xsl:if>
        <xsl:if test="@type">
            <xsl:for-each select="tokenize(@type,' ')">
                <xsl:variable name="type" select="."/>
                <xsl:choose>
                    <xsl:when test="not(substring-before($type,'#') = '')">
                        <xsl:apply-templates select="document(substring-before($type,'#'),$context)//wadl:resource_type[@id = substring-after(.,'#')]/wadl:method" mode="#current">
                            <xsl:with-param name="path" select="$path"/>
                            <xsl:with-param name="id" select="$id"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="$context//wadl:resource_type[@id = substring-after($type,'#')]/wadl:method" mode="#current">
                            <xsl:with-param name="path" select="$path"/>
                            <xsl:with-param name="id" select="$id"/>
                        </xsl:apply-templates>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="wadl:method[@href]">
        <xsl:param name="path">
            <xsl:for-each select="ancestor-or-self::wadl:resource/@path"><xsl:value-of select="."/><xsl:if test="not(ends-with(.,'/')) and not(position() = last())">/</xsl:if></xsl:for-each>
        </xsl:param>
        <xsl:param name="id" select="if (parent::wadl:resource/@id) then parent::wadl:resource/@id else '??ID_MISSING??'"/>
        <xsl:if test="not(parent::wadl:resource/@id)">
            <xsl:message>
                ID Missing in resource! <xsl:copy-of select="$path"/>
            </xsl:message>
        </xsl:if>
        <xsl:variable name="href" select="@href"/>
       <xsl:choose>
           <xsl:when test="not(substring-before(@href,'#') = '')">
               <xsl:apply-templates select="document(substring-before(@href,'#'),.)//wadl:method[@id = substring-after($href,'#')]">
                   <xsl:with-param name="path" select="$path"/>
                   <xsl:with-param name="id" select="$id"/>   
               </xsl:apply-templates>
           </xsl:when>
           <xsl:otherwise>
               <xsl:apply-templates select="//wadl:method[@id = substring-after($href,'#')]">
                   <xsl:with-param name="path" select="$path"/>
                   <xsl:with-param name="id" select="$id"/>   
               </xsl:apply-templates>               
           </xsl:otherwise>
       </xsl:choose>
    </xsl:template>
    
    <xsl:template match="wadl:method[@href]" mode="toc">
        <xsl:param name="path">
            <xsl:for-each select="ancestor-or-self::wadl:resource/@path"><xsl:value-of select="."/><xsl:if test="not(ends-with(.,'/')) and not(position() = last())">/</xsl:if></xsl:for-each>
        </xsl:param>
        <xsl:param name="id" select="parent::wadl:resource/@id"/>
        <xsl:variable name="href" select="@href"/>
        
        <xsl:choose>
            <xsl:when test="not(substring-before(@href,'#') = '')">
                <xsl:apply-templates select="document(substring-before(@href,'#'),.)//wadl:method[@id = substring-after($href,'#')]" mode="toc">
                    <xsl:with-param name="path" select="$path"/>
                    <xsl:with-param name="id" select="$id"/>   
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="//wadl:method[@id = substring-after($href,'#')]" mode="toc">
                    <xsl:with-param name="path" select="$path"/>
                    <xsl:with-param name="id" select="$id"/>   
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="wadl:method[not(@href)]" mode="toc">
        <xsl:param name="path"/>
        <xsl:param name="id"/>
        <xsl:param name="title">
            <xsl:choose>
                <xsl:when test="wadl:doc/@title"><xsl:value-of select="wadl:doc/@title"/> (<xsl:value-of select="$path"/>)</xsl:when>
                <xsl:otherwise><xsl:value-of select="concat($path, ' method id: ', @id)"/></xsl:otherwise>
            </xsl:choose>
        </xsl:param>
        <li><a href="#{$id}_{@id}"><xsl:value-of select="$title"/></a></li>
    </xsl:template>
    
    <xsl:template match="wadl:method[not(@href)]">
        <xsl:param name="path"/>
        <xsl:param name="id"/>
        <xsl:param name="title">
            <xsl:choose>
                <xsl:when test="wadl:doc/@title"><xsl:value-of select="wadl:doc/@title"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="concat($path, ' method id: ', @id)"/></xsl:otherwise>
            </xsl:choose>
        </xsl:param>
        <h2><xsl:value-of select="$title"/></h2>
        <anchor id="{$id}_{@id}"/>
        <pre class="brush: xml; highlight: [3, 4]">
            &lt;resources xmlns="http://wadl.dev.java.net/2009/02"&gt;
            &lt;!-- Path: <xsl:value-of select="$path"/> --&gt;
             &lt;resource href="<xsl:value-of select="concat($pathto, replace(base-uri(),'(.*/)(.*)', '$2'))"/>#<xsl:value-of select="$id"/>"&gt;
               &lt;method href="<xsl:value-of select="@id"/>">
                 &lt;wadl:doc xmlns:wadl="http://wadl.dev.java.net/2009/02" xmlns="http://docbook.org/ns/docbook">
                    &lt;!-- DocBook Markup is legal here -->
                    <xsl:if test="wadl:doc">&lt;!-- <xsl:if test="wadl:doc/@title"><xsl:value-of select="wadl:doc/@title"/>: </xsl:if><xsl:value-of select="substring(normalize-space(wadl:doc[1]),1,140)"/><xsl:if test="string-length(normalize-space(wadl:doc[1])) &gt; 140">...</xsl:if> --&gt;</xsl:if>
                 &lt;/wadl:doc>
               &lt;/method&gt;
             &lt;/resource&gt;
           &lt;/resources&gt;
        </pre>
    </xsl:template>
    
</xsl:stylesheet>