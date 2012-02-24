#! /bin/bash

# Copyright 2011 Rackspace US, Inc.

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Wrapper script for wadl normalization xslts.
#
# Author: david.cramer@rackspace.com
# Copyright 2011 Rackspace Hosting.

# This function figures out the location of the original script (as opposed to any chain of 
# symlinks pointing to it). Source: 
# http://muffinresearch.co.uk/archives/2008/10/10/bash-resolving-symlinks-to-shellscripts/
function resolve_symlink {
    SCRIPT=$1 NEWSCRIPT=''
    until [ "$SCRIPT" = "$NEWSCRIPT" ]; do
        if [ "${SCRIPT:0:1}" = '.' ]; then SCRIPT=$PWD/$SCRIPT; fi
        cd $(dirname $SCRIPT)
        if [ ! "${SCRIPT:0:1}" = '.' ]; then SCRIPT=$(basename $SCRIPT); fi
        SCRIPT=${NEWSCRIPT:=$SCRIPT}
        NEWSCRIPT=$(ls -l $SCRIPT | awk '{ print $NF }')
    done
    if [ ! "${SCRIPT:0:1}" = '/' ]; then SCRIPT=$PWD/$SCRIPT; fi
    echo $(dirname $SCRIPT)
}
DIR=$(resolve_symlink $0)

function saxonize {
    java \
        -jar "$DIR/../lib/saxon-9.1.0.8.jar" \
        -s:"$1" \
        -xsl:"$DIR/../xsl/$2" \
        -strip:all \
        -o:"$3" \
        format="$4-format" \
	$5 $6 $7 $8 $9 
    # Fail if the transformation failed.
    if [[ $? ]]
    then
	exit 0;
    fi

}

function USAGE()
{
    echo ""
    echo "Usage: $(basename $0) [-?dfvx] -w wadlFile"
    echo ""
    echo "OPTIONS:"
    echo "       -w wadlFile: The wadl file to normalize."
    echo "       -d omit or keep. Exclude/keep Wadl documentation. (keep by default)."     
    echo "       -f Wadl format. path or tree"
    echo "          path: Format resources in path format, "
    echo "                e.g. <resource path='foo/bar'/>"
    echo "          tree: Format resources in tree format, "
    echo "                e.g. <resource path='foo'><resource path='bar'>..."
    echo "          If you omit the -f switch, the script makes no "
    echo "          changes to the structure of the resources."
    echo "       -v XSD Version (1.0 and 1.1 supported, 1.1 is the default)"
    echo "       -x true or false. Flatten xsds (true by default)."
    echo "       -r keep or omit. Omit resource_type elements (keep by default)."
    exit 1
}

xsdVersion=1.1
flattenXsds=true
documentation=keep
resource_types=keep

#PROCESS ARGS
while getopts ":v:w:d:f:x:r:?" Option
do
    case $Option in
        v    ) xsdVersion=$OPTARG;;
        w    ) wadlFile=$OPTARG;;
        d    ) documentation=$OPTARG;;        
        f    ) wadlFormat=$OPTARG;;
        x    ) flattenXsds=$OPTARG;;
	    r    ) resource_types=$OPTARG;;
        ?    ) USAGE
               exit 0;;
        *    ) echo ""
               echo "Unimplemented option chosen."
               USAGE   # DEFAULT
    esac
done

if [[ -f "$wadlFile" && ( ! -n $wadlFormat || $wadlFormat = "path" || $wadlFormat = "tree" ) &&
(! -n $documentation || $documentation = "keep" || $documentation = "omit" )]]
then 
    [ -d "$(dirname $wadlFile)/normalized" ] || mkdir $(dirname $wadlFile)/normalized

    # Cleanup output of the last run
    #rm -f "$(dirname $wadlFile)/normalized/*"

    # Validate hte input wadl file against the wadl xsd.
    xmllint --noent --noout --schema "$DIR/../xsd/wadl.xsd"  $wadlFile
    [ $? -eq 0 ] || exit 1


    # Process the document wadl document.
    saxonize $wadlFile normalizeWadl.xsl $(dirname $wadlFile)/normalized/$(basename ${wadlFile%%.wadl}.wadl) "$wadlFormat" xsdVersion=$xsdVersion flattenXsds=$flattenXsds resource_types=$resource_types documentation=$documentation

    # Validate the output wadl.
    xmllint --noout --schema "$DIR/../xsd/wadl.xsd"  $(dirname $wadlFile)/normalized/$(basename ${wadlFile%%.wadl}.wadl)

    #[ $? -eq 0 ] || exit 1
    # Validate the generated xsds
    xmllint --noout --schema "$DIR/../xsd/XMLSchema${xsdVersion}.xsd"  $(dirname $wadlFile)/normalized/$(basename ${wadlFile%%.wadl}-xsd-*.xsd)
    #[ $? -eq 0 ] || exit 1
else
    USAGE;
fi
