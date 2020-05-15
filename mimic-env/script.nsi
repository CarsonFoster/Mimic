!include MUI2.nsh
Name "Mimic"
OutFile "mimic_install_1.0.exe"
Unicode True
InstallDir $DESKTOP
# need for removing Start Menu shortcut
RequestExecutionLevel user
ShowInstDetails show
ShowUninstDetails show

!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Header\win.bmp"
!define MUI_WELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\win.bmp"

!define MUI_WELCOMEPAGE_TITLE "Welcome!"
!define MUI_WELCOMEPAGE_TITLE_3LINES
!define MUI_WELCOMEPAGE_TEXT "Welcome to the Mimic installer! Mimic is a small, Java-based chat program for communicating over the local network. Press next to proceed with the installation."
!insertmacro MUI_PAGE_WELCOME

!define MUI_PAGE_HEADER_TEXT "License Agreement"
!define MUI_LICENSEPAGE_TEXT_TOP "Please read and agree to the GNU General Public License."
!define MUI_LICENSEPAGE_CHECKBOX
!define MUI_LICENSEPAGE_CHECKBOX_TEXT "I accept the terms and conditions of the license"
!define MUI_LICENSEPAGE_TEXT_BOTTOM "Press next to continue, once you have agreed to the terms and conditions."
!insertmacro MUI_PAGE_LICENSE LICENSE

!define MUI_PAGE_HEADER_TEXT "Component Selection"

!insertmacro MUI_PAGE_COMPONENTS

!define MUI_PAGE_HEADER_TEXT "Installation Directory Selection"
!insertmacro MUI_PAGE_DIRECTORY

!define MUI_PAGE_HEADER_TEXT "Installation Progress"
!insertmacro MUI_PAGE_INSTFILES

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

Section "Mimic Core" MimicCore
	SectionIn RO
	SetOutPath $INSTDIR\Mimic
	File /r /x *.nsi *.*
	WriteUninstaller $INSTDIR\Mimic\uninstall.exe
SectionEnd

Section "Start Menu Shortcut" StartMenuShortcut
	CreateShortcut "$SMPROGRAMS\Mimic.lnk" "$INSTDIR\Mimic\mimic.exe"
SectionEnd

Section "Desktop Shortcut" DesktopShortcut
	CreateShortcut "$DESKTOP\Mimic.lnk" "$INSTDIR\Mimic\mimic.exe"
SectionEnd

LangString DESC_MimicCore ${LANG_ENGLISH} "The actual Mimic application. This is required."
LangString DESC_StartMenuShortcut ${LANG_ENGLISH} "The Start Menu Shortcut to start Mimic directly."
LangString DESC_DesktopShortcut ${LANG_ENGLISH} "The Desktop shortcut to start Mimic directly."

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${MimicCore} $(DESC_MimicCore)
	!insertmacro MUI_DESCRIPTION_TEXT ${StartMenuShortcut} $(DESC_StartMenuShortcut)
	!insertmacro MUI_DESCRIPTION_TEXT ${DesktopShortcut} $(DESC_DesktopShortcut)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

Section "uninstall"
	# $INSTDIR will refer to the Mimic sub directory, so it's ok
	Delete $INSTDIR\uninstall.exe
	Delete $INSTDIR\release
	Delete $INSTDIR\README.MD
	Delete $INSTDIR\mimic.exe
	Delete $INSTDIR\LICENSE
	Delete $INSTDIR\default.properties
	Delete $INSTDIR\command.txt
	RMDir /r $INSTDIR\bin
	RMDir /r $INSTDIR\conf
	#RMDir /r $INSTDIR\dist
	RMDir /r $INSTDIR\legal
	RMDir /r $INSTDIR\lib
	IfFileExists $SMPROGRAMS\Mimic.lnk 0 +2
		Delete $SMPROGRAMS\Mimic.lnk
	IfFileExists $DESKTOP\Mimic.lnk 0 +2
		Delete $DESKTOP\Mimic.lnk
	RMDir $INSTDIR
SectionEnd