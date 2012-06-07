viewclient
==========

This is a demo app that shows a JSF client of the AS7 management model and specifically, the views subsystem.

It shows the ability take view definitions created in CLI GUI and render them in JSF.

For each attribute displayed, it looks at the management model to see if it is writable.  If so, it provides the ability to do in-place editing.

Currently, this client only works on standalone.