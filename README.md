# GMAIL Forwarder

This project allow to automatically forward emails that arrive in a particular GMAIL IMAP folder to a list of recipients.
**Motivation:** allow to transfer email without configuring the email forward feature of GMAIL and also works for email created by API (not the case for the GMAIL forward feature).

## DOCKER usage

The configuration comes from environment variables, no configuration file needed.

```
docker run --name gmail-forwarder -d -e USERNAME=gmailaccount -e PASSWORD=gmailpassword -e TO=recipient@mail.com nicolasalbert/gmail-forwarder
```

## GRADLE usage

Same as DOCKER, configuration is read from environment variables. Once done, via your shell or a script, just launch the application.

```
gradlew run
```

## CONFIGURATION

Here the list of environment variable used:

* **USERNAME**: mandatory GMAIL account that receive emails
* **PASSWORD**: mandatory GMAIL account's password or access key
* **TO**: mandatory recipient email (use , to set many)
* **FOLDER**: folder to watch for new email, default is the **Inbox**
* **SENDER**: if the sender of an email matches (find) this regular expression, the email can be forwarded
* **SUBJECT**: if the subject of an email matches (find) this regular expression, the email can be forwarded