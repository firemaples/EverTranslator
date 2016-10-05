#!/bin/sh
#
# Original from https://help.github.com/articles/changing-author-info/
# Edit by firemaples

PROJECT_OWNER="firemaples"
PROJECT_NAME="OnScTrTest"

OLD_EMAIL="louis1chen@truetel.com"
CORRECT_NAME="firemaples"
CORRECT_EMAIL="firemaples@gmail.com"

function pauseWait(){
	while [[ true ]]; do
	    read -n1 -r -p "Press [r] to continue..." key
	    if [[ "$key" == 'r' ]];then
			echo
	        echo "[r] key pressed, bash continue"
			echo
			break
	    fi
	done
}

echo
echo
echo "********************************"
echo "******** Git bare clone ********"
echo "********************************"
cd ..

if [ -d "$PROJECT_NAME.git" ]; then
	echo
	echo "### Folder $PROJECT_NAME.git is existed, press [r] to remove the folder ###"
	echo
	
	pauseWait
	rm -rf $PROJECT_NAME.git
fi

git clone --bare "https://github.com/$PROJECT_OWNER/$PROJECT_NAME.git"

cd "$PROJECT_NAME.git"

echo
echo
echo "********************************"
echo "***** Replace name & email *****"
echo "********************************"
git filter-branch --env-filter '
OLD_EMAIL="louis1chen@truetel.com"
CORRECT_NAME="firemaples"
CORRECT_EMAIL="firemaples@gmail.com"
if [ "$GIT_COMMITTER_EMAIL" = "$OLD_EMAIL" ]
then
    export GIT_COMMITTER_NAME="$CORRECT_NAME"
    export GIT_COMMITTER_EMAIL="$CORRECT_EMAIL"
fi
if [ "$GIT_AUTHOR_EMAIL" = "$OLD_EMAIL" ]
then
    export GIT_AUTHOR_NAME="$CORRECT_NAME"
    export GIT_AUTHOR_EMAIL="$CORRECT_EMAIL"
fi
' --tag-name-filter cat -- --branches --tags

echo
echo
echo "### Please check the changes, then press [r] button to continue ###"
echo
echo

pauseWait

echo
echo
echo "********************************"
echo "********* Push changes *********"
echo "********************************"
git push --force --tags origin 'refs/heads/*'

echo
echo
echo "********************************"
echo "***** Clean up temp clone ******"
echo "********************************"
cd ..
rm -rf $PROJECT_NAME.git

echo
echo
echo "#### FINISHED ####"
echo
echo