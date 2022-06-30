// ----------------------------------------------------------------------------
// Copyright 2007-2020, GeoTelematic Solutions, Inc.
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Change History:
//  02/19/2006  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opengts.util;

public interface PropertyProvider
{

    /**
    *** Returns a property value
    *** @param key The property key
    *** @param dft The default value to return if the key was not found
    *** @return The property value
    **/
    public Object getProperty(Object key, Object dft);

}
