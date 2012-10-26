<?xml version="1.0" ?>

<xsl:stylesheet
   version="2.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias"
>

<xsl:import href="iso_svrl_for_xslt2.xsl"/>

<xsl:namespace-alias stylesheet-prefix="axsl" result-prefix="xsl"/>

<xsl:template name="process-prolog">
    <axsl:template match="@*|node()" mode="#all">
        <axsl:apply-templates select="@*|node()" mode="#current"/>
    </axsl:template>
</xsl:template>

</xsl:stylesheet>
