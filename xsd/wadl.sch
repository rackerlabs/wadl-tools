<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
        queryBinding='xslt2'>
    <title>WADL Assertions</title>
    <ns prefix="wadl" uri="http://wadl.dev.java.net/2009/02"/>
    <let name="baseDocURI" value="string-join(tokenize(document-uri(/),'/')[position() ne last()], '/')"/>
    <pattern id="References">
        <rule id="CheckReference" abstract="true">
            <let name="doc" value="substring-before(.,'#')"/>
            <let name="ref" value="substring-after(.,'#')"/>
            <let name="attRef" value="if (string-length($doc) != 0) then document(resolve-uri($doc,concat($baseDocURI,'/')))/wadl:application//@id[.=$ref] else //@id[.=$ref]"/>
            <assert test="contains(., '#')">
                Reference is missing '#'.
            </assert>
            <assert test="$attRef">
                The reference does not seem to exist.
            </assert>
        </rule>
        <rule id="CheckReferences" abstract="true">
            <let name="remoteids" value="
                for 
                    $refs in tokenize(normalize-space(.),' ')[not(substring-before(.,'#') = '')] 
                return    
                    $refs
                "/>
            <let name="localids" value="
                for 
                $refs in tokenize(normalize-space(.),' ')[substring-before(.,'#') = ''] 
                return    
                    $refs
                "/>
            <let name="localAttRef" value="every $id in $localids satisfies (//@id[. = substring-after($id,'#')])"/>
            <let name="remoteAttRef" value="every $id in $remoteids satisfies (document(resolve-uri(substring-before($id,'#'),concat($baseDocURI,'/')))/wadl:application//@id[.= substring-after($id,'#')])"/>
            <assert test="every $ref in tokenize(normalize-space(.),' ') satisfies contains($ref, '#')">
                Reference is missing '#'.
            </assert>
            <assert test="$remoteAttRef">
                A reference listed in the type attribute does not seem to exist in another wadl.
            </assert>
            <assert test="$localAttRef">
                A reference listed in the type attribute does not seem to exist in this wadl.
            </assert>
        </rule>
        <rule context="wadl:resource/@type">
            <extends rule="CheckReferences"/>
        </rule>
        <rule context="@resource_type" >
            <extends rule="CheckReference"/>
            <assert test="name($attRef/..)='resource_type'">
                The reference should map to a resource_type.
            </assert>
        </rule>
        <rule context="wadl:method/@href">
            <extends rule="CheckReference"/>
            <assert test="name($attRef/..)='method'">
                The reference should map to a method.
            </assert>
        </rule>
        <rule context="wadl:representation/@href">
            <extends rule="CheckReference"/>
            <assert test="name($attRef/..)='representation'">
                The reference should map to a representation.
            </assert>
        </rule>
        <rule context="wadl:param/@href">
            <extends rule="CheckReference"/>
            <assert test="name($attRef/..)='param'">
                The reference should map to a param.
            </assert>
        </rule>
        <rule context="wadl:include/@href">
            <assert test="doc-available(resolve-uri(.,concat($baseDocURI,'/')))">
                Include file is not available.
            </assert>
        </rule>
    </pattern>
</schema>
