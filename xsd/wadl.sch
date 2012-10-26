<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
        queryBinding='xslt2'>
    <title>WADL Assertions</title>
    <ns prefix="wadl" uri="http://wadl.dev.java.net/2009/02"/>
    <ns prefix="xsdxt" uri="http://docs.rackspacecloud.com/xsd-ext/v1.0"/>
    <pattern id="References">
        <rule id="CheckReference" abstract="true">
            <let name="doc" value="substring-before(.,'#')"/>
            <let name="ref" value="substring-after(.,'#')"/>
            <let name="baseDocURI" value="string-join(tokenize(base-uri(..),'/')[position() ne last()], '/')"/>
            <let name="attRef" value="if (string-length($doc) != 0) then document(resolve-uri($doc,concat($baseDocURI,'/')))/wadl:application//@id[.=$ref] else //@id[.=$ref]"/>
            <assert test="contains(., '#')">
                The reference '<value-of select="."/>' is missing '#'.
            </assert>
            <assert test="$attRef">
                The reference '<value-of select="."/>' does not seem to exist.
            </assert>
        </rule>
        <rule id="CheckReferences" abstract="true">
            <let name="baseDocURI" value="string-join(tokenize(base-uri(..),'/')[position() ne last()], '/')"/>
            <let name="ids" value="tokenize(normalize-space(.),' ')"/>
            <let name="remoteids" value="
                for 
                    $refs in $ids[not(substring-before(.,'#') = '')]
                return    
                    $refs
                "/>
            <let name="localids" value="
                for 
                $refs in $ids[substring-before(.,'#') = '']
                return    
                    $refs
                "/>
            <let name="localAttRef" value="every $id in $localids satisfies (//@id[. = substring-after($id,'#')])"/>
            <let name="remoteAttRef" value="every $id in $remoteids satisfies (document(resolve-uri(substring-before($id,'#'),concat($baseDocURI,'/')))/wadl:application//@id[.= substring-after($id,'#')])"/>
            <assert test="every $ref in $ids satisfies contains($ref, '#')">
                In the set of references '<value-of select="."/>', the following references '<value-of select="$ids[not(contains(.,'#'))]" separator="' '"/>' are missing '#'.
            </assert>
            <assert test="$remoteAttRef">
                In the set of refereces '<value-of select="."/>', the following external references '<value-of select="for $id in $remoteids return if (not(document(resolve-uri(substring-before($id,'#'),concat($baseDocURI,'/')))/wadl:application//@id[.= substring-after($id,'#')])) then $id else ()" separator="' '"/>' do not seem to exist.
            </assert>
            <assert test="$localAttRef">
                In the set of references '<value-of select="."/>', the following references '<value-of select="for $id in $localids return if (not(//@id[. = substring-after($id,'#')])) then $id else ()" separator="' '"/>' do not seem to exist in this wadl.
            </assert>
        </rule>
        <rule context="wadl:resource/@type">
            <extends rule="CheckReferences"/>
            <assert test="every $id in $localids satisfies (//@id[(. = substring-after($id,'#')) and (local-name(..)='resource_type')])">
                In the set of references '<value-of select="."/>', the following references '<value-of select="for $id in $localids return if (//@id[(. = substring-after($id,'#')) and (local-name(..)='resource_type')]) then () else $id" seperator="' '"/>' are not pointing to a resource type.
            </assert>
            <assert test="every $id in $remoteids satisfies (document(resolve-uri(substring-before($id,'#'),concat($baseDocURI,'/')))/wadl:application//@id[.= substring-after($id,'#') and (local-name(..)='resource_type')])">
                In the set of references '<value-of select="."/>', the following external references '<value-of select="for $id in $remoteids return if (document(resolve-uri(substring-before($id,'#'),concat($baseDocURI,'/')))/wadl:application//@id[.= substring-after($id,'#') and (local-name(..)='resource_type')]) then () else $id" seperator="' '"/>' are not pointing to a resource type.
            </assert>
        </rule>
        <rule context="wadl:link/@resource_type" >
            <extends rule="CheckReference"/>
            <assert test="local-name($attRef/..)='resource_type'">
                The reference '<value-of select="."/>' should point to a resource_type.
            </assert>
        </rule>
        <rule context="wadl:method/@href">
            <extends rule="CheckReference"/>
            <assert test="local-name($attRef/..)='method'">
                The reference '<value-of select="."/>' should point to a method.
            </assert>
        </rule>
        <rule context="wadl:representation/@href">
            <extends rule="CheckReference"/>
            <assert test="local-name($attRef/..)='representation'">
                The reference '<value-of select="."/>' should point to a representation.
            </assert>
        </rule>
        <rule context="wadl:param/@href">
            <extends rule="CheckReference"/>
            <assert test="local-name($attRef/..)='param'">
                The reference '<value-of select="."/>' point to a param.
            </assert>
        </rule>
        <rule context="wadl:include/@href">
            <assert test="doc-available(resolve-uri(.,base-uri(..)))">
                Included file '<value-of select="."/>' is not available.
            </assert>
        </rule>
        <rule context="xsdxt:code/@href">
            <assert test="unparsed-text-available(resolve-uri(.,base-uri(..)))">
                The code sample '<value-of select="."/>' is not available.
            </assert>
        </rule>
    </pattern>
</schema>
