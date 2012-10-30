<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:wadl="http://wadl.dev.java.net/2009/02"
    xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
    xmlns="http://docs.rackspace.com/api"
    exclude-result-prefixes="xs wadl"
    version="2.0">

    <xsl:import href="wadl.xsl"/>

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <svrl:schematron-output
            title="WADL Assertions"
            schemaVersion="">
            <xsl:comment>
                <xsl:value-of select="$archiveDirParameter"/>
                <xsl:value-of select="$archiveNameParameter"/>
                <xsl:value-of select="$fileNameParameter"/>
                <xsl:value-of select="$fileDirParameter"/>
            </xsl:comment>

            <svrl:ns-prefix-in-attribute-values uri="http://wadl.dev.java.net/2009/02" prefix="wadl"/>
            <svrl:ns-prefix-in-attribute-values uri="http://docs.rackspacecloud.com/xsd-ext/v1.0" prefix="xsdxt"/>

            <xsl:call-template name="process_doc"/>
        </svrl:schematron-output>
    </xsl:template>

    <xsl:template name="process_doc">
        <xsl:param name="doc" as="node()" select="."/>
        <xsl:param name="excludes" as="xs:string*" select="()"/>
        <xsl:param name="nextLinks" as="xs:string*" select="()"/>
        <xsl:variable name="doc_uri" as="xs:string" select="string(document-uri($doc))"/>

        <!-- Run Validation Rules on DOC -->
        <xsl:variable name="newNextLinks" as="xs:string*" select="($nextLinks, wadl:nextLinks($doc,($excludes, $doc_uri)))"/>
        <xsl:variable name="newExcludes" as="xs:string*" select="($excludes, $doc_uri)"/>

        <xsl:message>[INFO] Checking: <xsl:value-of select="$doc_uri"/></xsl:message>
        <xsl:message>[INFO] Next: <xsl:value-of select="$newNextLinks" separator=", "/></xsl:message>
        <xsl:message>[INFO] Excluding: <xsl:value-of select="$newExcludes" separator=", "/></xsl:message>


        <svrl:active-pattern>
            <xsl:attribute name="document">
                <xsl:value-of select="$doc_uri"/>
            </xsl:attribute>
            <xsl:attribute name="id">References</xsl:attribute>
            <xsl:attribute name="name">References</xsl:attribute>
            <xsl:apply-templates select="$doc/*"/>
        </svrl:active-pattern>

        <xsl:apply-templates select="$doc" mode="M4"/>

        <!-- Test next document -->
        <xsl:if test="$newNextLinks">
            <xsl:call-template name="process_doc">
                <xsl:with-param name="excludes" select="$newExcludes"/>
                <xsl:with-param name="nextLinks" select="$newNextLinks[position() != 1]"/>
                <xsl:with-param name="doc" select="document($newNextLinks[1], $doc)"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:function name="wadl:nextLinks" as="xs:string*">
        <xsl:param name="doc" as="node()"/>
        <xsl:param name="excludes" as="xs:string*"/>
        <xsl:variable name="nextLinks">
            <xsl:apply-templates select="$doc" mode="gatherLinks">
                <xsl:with-param name="doc" select="$doc" as="node()" tunnel="yes"/>
                <xsl:with-param name="excludes" select="$excludes" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:variable>

        <xsl:for-each-group select="$nextLinks/*" group-by="@href">
            <xsl:value-of select="current-group()[1]/@href"/>
        </xsl:for-each-group>
    </xsl:function>

    <xsl:template match="wadl:resource[@type]" mode="gatherLinks">
        <xsl:param name="doc" as="node()" tunnel="yes"/>
        <xsl:param name="excludes" tunnel="yes" as="xs:string*"/>
        <xsl:variable name="types" as="xs:string*" select="tokenize(@type,' ')"/>
        <xsl:for-each select="$types">
            <xsl:call-template name="check_wadl_href">
                <xsl:with-param name="href" select="."/>
                <xsl:with-param name="excludes" select="$excludes"/>
                <xsl:with-param name="doc" select="$doc"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="xs:*/@schemaLocation" mode="gatherLinks">
        <xsl:param name="doc" as="node()" tunnel="yes"/>
        <xsl:param name="excludes" as="xs:string*" tunnel="yes"/>
        <xsl:call-template name="check_href">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="href" select="."/>
            <xsl:with-param name="excludes" select="$excludes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="wadl:*/@href" mode="gatherLinks">
        <xsl:param name="doc" as="node()" tunnel="yes"/>
        <xsl:param name="excludes" tunnel="yes" as="xs:string*"/>
        <xsl:call-template name="check_wadl_href">
            <xsl:with-param name="href" select="."/>
            <xsl:with-param name="excludes" select="$excludes"/>
            <xsl:with-param name="doc" select="$doc"/>
        </xsl:call-template>
    </xsl:template>

    <!--
       A WADL included is a special case, it's handled like a schema reference.
    -->
    <xsl:template match="wadl:include/@href" mode="gatherLinks" priority="2">
        <xsl:param name="doc" as="node()" tunnel="yes"/>
        <xsl:param name="excludes" as="xs:string*" tunnel="yes"/>
        <xsl:call-template name="check_href">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="href" select="."/>
            <xsl:with-param name="excludes" select="$excludes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="check_wadl_href">
        <xsl:param name="doc" as="node()"/>
        <xsl:param name="href" as="xs:string"/>
        <xsl:param name="excludes" as="xs:string*"/>
        <xsl:if test="contains($href,'#')">
            <xsl:variable name="file" select="substring-before($href,'#')"/>
            <xsl:call-template name="check_href">
                <xsl:with-param name="doc" select="$doc"/>
                <xsl:with-param name="href" select="$file"/>
                <xsl:with-param name="excludes" select="$excludes"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="check_href">
        <xsl:param name="doc" as="node()"/>
        <xsl:param name="href" as="xs:string"/>
        <xsl:param name="excludes" as="xs:string*"/>
        <xsl:variable name="full-path" select="resolve-uri($href, document-uri($doc))"></xsl:variable>
        <xsl:if test="not($full-path = $excludes) and doc-available($full-path)">
            <link><xsl:attribute name="href"><xsl:value-of select="$full-path"/></xsl:attribute></link>
        </xsl:if>
    </xsl:template>

    <xsl:template match="text()" mode="#all"/>
</xsl:stylesheet>
