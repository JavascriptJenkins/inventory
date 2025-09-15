## get the user permissions
id techvvs

## check the shell permissions
grep techvvs /etc/passwd

## generate the ssh keys locally
ssh-keygen -t rsa -b 4096 -C "techvvs@techvvs.io" -f ~/.ssh/id_rsa_techvvs
## generate using better algorithim
ssh-keygen -t ed25519 -C "techvvs@techvvs.io" -f ~/.ssh/id_ed2_techvvs2


## go on the remote host and change user to techvvs
sudo su - techvvs
mkdir -p ~/.ssh
chmod 700 ~/.ssh

## manually copy the key from local
cat ~/.ssh/id_rsa_techvvs.pub
cat ~/.ssh/id_ed2_techvvs.pub
cat ~/.ssh/id_ed2_techvvs2.pub

## go to remote host and add it (as the techvvs user) and paste key in
nano ~/.ssh/authorized_keys

## on remote server as techvvs chmod the keys
chmod 600 ~/.ssh/authorized_keys
chown -R techvvs:techvvs ~/.ssh

## restart the ssh service on remote host
sudo systemctl restart ssh

## now from local macbook, login to server
ssh -i ~/.ssh/id_rsa_techvvs techvvs@198.199.72.34
ssh -i ~/.ssh/id_ed2_techvvs techvvs@198.199.72.34
ssh -i ~/.ssh/id_ed2_techvvs2 techvvs@157.230.91.190



## copy the id to the remote host where it will be used
#ssh-copy-id -i ~/.ssh/id_rsa_techvvs.pub techvvs@198.199.72.34
#ssh-copy-id -i ~/.ssh/id_ed2_techvvs.pub techvvs@198.199.72.34

## now it should work! home dir on linux is /home/techvvs

## now go run the serversetup.sh if you have not already