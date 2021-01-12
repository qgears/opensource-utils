#ARCHPOSTFIX=`uname -m | sed -e 's/\bx86\b/i686/'` | sed -e 's/\bx86\b/i686/'`
OS_RELEASE_ID=`grep -w "ID" /etc/os-release | cut -d'=' -f2`
OS_RELEASE_VERSION=`grep -w "VERSION_ID" /etc/os-release | cut -d'=' -f2 | tr -d '\"'`
echo ${OS_RELEASE_ID}-${OS_RELEASE_VERSION}-${ARCHPOSTFIX}
