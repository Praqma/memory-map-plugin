#!/usr/bin/env ruby
# encoding: utf-8
require 'open3'
require 'docopt'
require "pp"
require 'fileutils'
require 'rubygems'
require 'nokogiri'
require 'open-uri'
   

doc = <<DOCOPT
Check there is a changelog entry on URL that matches the current project version in the pom (if removing snapshot).

pom.xml
...
<version>2.2.3-SNAPSHOT</version>
...

    then there must be a changelog entry on URL (could be https://wiki.jenkins-ci.org/display/JENKINS/Pretested+Integration+Plugin):

...
h5. Version 2.2.3
...

Script could of course be improved to to take regexp to look for etc.

Usage:
  #{__FILE__} URL
  #{__FILE__} -h

example:
    ./check_for_changelog.rb https://wiki.jenkins-ci.org/display/JENKINS/Pretested+Integration+Plugin

Arguments:
  URL            URL to look for changelog entry "h5. Version %VERSION", where %VERSION matches version in pom xml

Options:
  -h --help         show this help message and exit

DOCOPT


if __FILE__ == $0
	begin
		params = Docopt::docopt(doc)
		pp params

		version = "none"
		result = false
		found = false
		filename="pom.xml"
		max_lines_to_check = 30 # only check first 30 lines, and the project version is there
		File.open(filename, "r").each_line do |line|
			if (max_lines_to_check < 0) then 
				break
			end
			if  mymatch = line.match('<version>([0-9]+\.[0-9]+\.[0-9]+)-SNAPSHOT</version>') then
				# matchdata returned:
				#pp mymatch[0] # matches the hole line
				#pp mymatch[1] # matches the grouping around the version number
				if mymatch[1].match(/[0-9]+\.[0-9]+\.[0-9]+/) then ## extra check
      					# This how the plugin need the environment variables
					found = true
					version = mymatch[1]
					pp "Found version number in pom to be: #{ version }"
					break
				end
				#pp line
				max_lines_to_check = max_lines_to_check - 1
    			end
		end

        


        # https://blog.engineyard.com/2010/getting-started-with-nokogiri
		page = Nokogiri::HTML(open(params["URL"]))
        # Find all "a" tags with a parent tag whose name is "h5"
        # as we know h3 is the version number headers in the changelog
        # <h5><a name="PretestedIntegrationPlugin-Version2.2.3"></a>Version 2.2.3</h5>
        
        page.xpath('//h5/a').each do |node|
            # nodes look like this:
            #   #(Element:0x10970bc {
            #       name = "a",
            #       attributes = [
            #           #(Attr:0x1096d4c {
            #               name = "name",
            #               value = "PretestedIntegrationPlugin-Version2.2.3"
            #           })]
            #   })

            # node["name"] will look like: PretestedIntegrationPlugin-Version2.2.3
            if mymatch = /PretestedIntegrationPlugin-Version([\d|\.]+.*)/.match(node["name"]) then
                if mymatch[1] == version then
                    pp "Found match with version and changelog entry on web page - great job!"
                    result = true
                end
            end
        end

		if not result then
			abort("Could find any changelog entry on the url - please create a changelog")
		end
	
	rescue Docopt::Exit => e
		puts "ERROR - #{ __FILE__ } - wrong usage.\n" << e.message
		abort() # needed for non zero exit code
	end
end
