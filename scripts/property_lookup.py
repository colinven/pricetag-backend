#!/usr/bin/env python3
"""
Property Lookup Script
Pulls sqft, year built, # of stories, garage size, property type, latitude, longitude
"""

import json, sys, pandas

def main():

    # handle invalid argument length
    if len(sys.argv) != 2:
        sys.stderr.write(f"argument length invalid, correct arguments passed?\n")
        sys.exit(1)

    address = sys.argv[1]

    if address == "null":
        sys.stderr.write("empty/null argument passed")
        sys.exit(1)

    # handle cases where homeharvest is not installed
    try:
        from homeharvest import scrape_property
    except ImportError as e:
        sys.stderr.write(f"homeharvest not installed.\n")
        sys.exit(1)

    try:
        props = scrape_property(location=address, listing_type=None)
        if not props.empty:
            result = props.iloc[0]
    except Exception as e:
        sys.stderr.write(f"failed to scrape property data: {e}\n")
        sys.exit(1)

    sqft = result.get("sqft")
    year_built = result.get("year_built")
    stories = result.get("stories")
    garage = result.get("parking_garage")
    property_type = result.get("style")

    sys.stdout.write(json.dumps({
        "sqft": None if pandas.isna(sqft) else int(sqft), 
        "year_built": None if pandas.isna(year_built) else int(year_built), 
        "stories": None if pandas.isna(stories) else int(stories),
        "garage": None if pandas.isna(garage) else int(garage),
        "property_type": property_type or None,
        }))

if __name__ == "__main__":
    main()