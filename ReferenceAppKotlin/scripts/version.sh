#!/bin/sh
# Copyright 2023 defsub
#
# This file is part of Takeout.
#
# Takeout is free software: you can redistribute it and/or modify it under the
# terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option)
# any later version.
#
# Takeout is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for
# more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with Takeout.  If not, see <https://www.gnu.org/licenses/>.

number="[0-9]+\.[0-9]+\.[0-9]+"
pattern="(${number})(.+#version#)"

file=.version
dryrun=0
version=""

while getopts x:f:th flag
do
    case "${flag}" in
	x) version=${OPTARG};;
	f) file=${OPTARG};;
	t) dryrun=1;;
	h)
	    echo "Usage:"
	    echo "  $0 [flags]"
	    echo
	    echo "Flags:"
	    echo "  -f file    ; version file, default is .version"
	    echo "  -x version ; override version file"
	    echo "  -d         ; dryrun"
	    exit 0
	    ;;
	*) exit 1;;
    esac
done

if test -z "${version}"
then
    # ensure file exists
    if ! test -f $file
    then
	echo "${file}: does not exist"
	exit 1
    fi
    version=`cat ${file}`
fi

# ensure version is provided
if test -z "${version}"
then
    echo "${file}: version is empty"
    exit 1
fi
# ensure version is valid
if echo $version | egrep -E "^${number}$"
then
    echo "using ${version}"
else
    echo "invalid version number: ${version}"
    exit 1
fi

# find files with version patterns
find . -type f | xargs egrep -l -E "${pattern}" | while read f
do
    if test ${dryrun} -eq 1
    then
	# dryrun print only
	result=`sed -E -e "s/${pattern}/${version}\2/g" $f`
	echo "${f}: ${result}"
    else
	# update file
	sed -i -E -e "s/${pattern}/${version}\2/g" $f
	echo "${f}: updated"
    fi
done
