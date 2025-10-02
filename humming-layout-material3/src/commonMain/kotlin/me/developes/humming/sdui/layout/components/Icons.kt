package me.developes.humming.sdui.layout.components

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.Activity
import compose.icons.feathericons.Airplay
import compose.icons.feathericons.AlertCircle
import compose.icons.feathericons.AlertOctagon
import compose.icons.feathericons.AlertTriangle
import compose.icons.feathericons.AlignCenter
import compose.icons.feathericons.AlignJustify
import compose.icons.feathericons.AlignLeft
import compose.icons.feathericons.AlignRight
import compose.icons.feathericons.Anchor
import compose.icons.feathericons.Aperture
import compose.icons.feathericons.Archive
import compose.icons.feathericons.ArrowDown
import compose.icons.feathericons.ArrowDownCircle
import compose.icons.feathericons.ArrowDownLeft
import compose.icons.feathericons.ArrowDownRight
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.ArrowLeftCircle
import compose.icons.feathericons.ArrowRight
import compose.icons.feathericons.ArrowRightCircle
import compose.icons.feathericons.ArrowUp
import compose.icons.feathericons.ArrowUpCircle
import compose.icons.feathericons.ArrowUpLeft
import compose.icons.feathericons.ArrowUpRight
import compose.icons.feathericons.AtSign
import compose.icons.feathericons.Award
import compose.icons.feathericons.BarChart
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.Battery
import compose.icons.feathericons.BatteryCharging
import compose.icons.feathericons.Bell
import compose.icons.feathericons.BellOff
import compose.icons.feathericons.Bluetooth
import compose.icons.feathericons.Bold
import compose.icons.feathericons.Book
import compose.icons.feathericons.BookOpen
import compose.icons.feathericons.Bookmark
import compose.icons.feathericons.Box
import compose.icons.feathericons.Briefcase
import compose.icons.feathericons.Calendar
import compose.icons.feathericons.Camera
import compose.icons.feathericons.CameraOff
import compose.icons.feathericons.Cast
import compose.icons.feathericons.Check
import compose.icons.feathericons.CheckCircle
import compose.icons.feathericons.CheckSquare
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.ChevronLeft
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.ChevronUp
import compose.icons.feathericons.ChevronsDown
import compose.icons.feathericons.ChevronsLeft
import compose.icons.feathericons.ChevronsRight
import compose.icons.feathericons.ChevronsUp
import compose.icons.feathericons.Chrome
import compose.icons.feathericons.Circle
import compose.icons.feathericons.Clipboard
import compose.icons.feathericons.Clock
import compose.icons.feathericons.Cloud
import compose.icons.feathericons.CloudDrizzle
import compose.icons.feathericons.CloudLightning
import compose.icons.feathericons.CloudOff
import compose.icons.feathericons.CloudRain
import compose.icons.feathericons.CloudSnow
import compose.icons.feathericons.Code
import compose.icons.feathericons.Codepen
import compose.icons.feathericons.Codesandbox
import compose.icons.feathericons.Coffee
import compose.icons.feathericons.Columns
import compose.icons.feathericons.Command
import compose.icons.feathericons.Compass
import compose.icons.feathericons.Copy
import compose.icons.feathericons.CornerDownLeft
import compose.icons.feathericons.CornerDownRight
import compose.icons.feathericons.CornerLeftDown
import compose.icons.feathericons.CornerLeftUp
import compose.icons.feathericons.CornerRightDown
import compose.icons.feathericons.CornerRightUp
import compose.icons.feathericons.CornerUpLeft
import compose.icons.feathericons.CornerUpRight
import compose.icons.feathericons.Cpu
import compose.icons.feathericons.CreditCard
import compose.icons.feathericons.Crop
import compose.icons.feathericons.Crosshair
import compose.icons.feathericons.Database
import compose.icons.feathericons.Delete
import compose.icons.feathericons.Disc
import compose.icons.feathericons.Divide
import compose.icons.feathericons.DivideCircle
import compose.icons.feathericons.DivideSquare
import compose.icons.feathericons.DollarSign
import compose.icons.feathericons.Download
import compose.icons.feathericons.DownloadCloud
import compose.icons.feathericons.Dribbble
import compose.icons.feathericons.Droplet
import compose.icons.feathericons.Edit
import compose.icons.feathericons.Edit2
import compose.icons.feathericons.Edit3
import compose.icons.feathericons.ExternalLink
import compose.icons.feathericons.Eye
import compose.icons.feathericons.EyeOff
import compose.icons.feathericons.Facebook
import compose.icons.feathericons.FastForward
import compose.icons.feathericons.Feather
import compose.icons.feathericons.Figma
import compose.icons.feathericons.File
import compose.icons.feathericons.FileMinus
import compose.icons.feathericons.FilePlus
import compose.icons.feathericons.FileText
import compose.icons.feathericons.Film
import compose.icons.feathericons.Filter
import compose.icons.feathericons.Flag
import compose.icons.feathericons.Folder
import compose.icons.feathericons.FolderMinus
import compose.icons.feathericons.FolderPlus
import compose.icons.feathericons.Framer
import compose.icons.feathericons.Frown
import compose.icons.feathericons.Gift
import compose.icons.feathericons.GitBranch
import compose.icons.feathericons.GitCommit
import compose.icons.feathericons.GitMerge
import compose.icons.feathericons.GitPullRequest
import compose.icons.feathericons.Github
import compose.icons.feathericons.Gitlab
import compose.icons.feathericons.Globe
import compose.icons.feathericons.Grid
import compose.icons.feathericons.HardDrive
import compose.icons.feathericons.Hash
import compose.icons.feathericons.Headphones
import compose.icons.feathericons.Heart
import compose.icons.feathericons.HelpCircle
import compose.icons.feathericons.Hexagon
import compose.icons.feathericons.Home
import compose.icons.feathericons.Image
import compose.icons.feathericons.Inbox
import compose.icons.feathericons.Info
import compose.icons.feathericons.Instagram
import compose.icons.feathericons.Italic
import compose.icons.feathericons.Key
import compose.icons.feathericons.Layers
import compose.icons.feathericons.Layout
import compose.icons.feathericons.LifeBuoy
import compose.icons.feathericons.Link
import compose.icons.feathericons.Link2
import compose.icons.feathericons.Linkedin
import compose.icons.feathericons.List
import compose.icons.feathericons.Loader
import compose.icons.feathericons.Lock
import compose.icons.feathericons.LogIn
import compose.icons.feathericons.LogOut
import compose.icons.feathericons.Mail
import compose.icons.feathericons.Map
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.Maximize
import compose.icons.feathericons.Maximize2
import compose.icons.feathericons.Meh
import compose.icons.feathericons.Menu
import compose.icons.feathericons.MessageCircle
import compose.icons.feathericons.MessageSquare
import compose.icons.feathericons.Mic
import compose.icons.feathericons.MicOff
import compose.icons.feathericons.Minimize
import compose.icons.feathericons.Minimize2
import compose.icons.feathericons.Minus
import compose.icons.feathericons.MinusCircle
import compose.icons.feathericons.MinusSquare
import compose.icons.feathericons.Monitor
import compose.icons.feathericons.Moon
import compose.icons.feathericons.MoreHorizontal
import compose.icons.feathericons.MoreVertical
import compose.icons.feathericons.MousePointer
import compose.icons.feathericons.Move
import compose.icons.feathericons.Music
import compose.icons.feathericons.Navigation
import compose.icons.feathericons.Navigation2
import compose.icons.feathericons.Octagon
import compose.icons.feathericons.Package
import compose.icons.feathericons.Paperclip
import compose.icons.feathericons.Pause
import compose.icons.feathericons.PauseCircle
import compose.icons.feathericons.PenTool
import compose.icons.feathericons.Percent
import compose.icons.feathericons.Phone
import compose.icons.feathericons.PhoneCall
import compose.icons.feathericons.PhoneForwarded
import compose.icons.feathericons.PhoneIncoming
import compose.icons.feathericons.PhoneMissed
import compose.icons.feathericons.PhoneOff
import compose.icons.feathericons.PhoneOutgoing
import compose.icons.feathericons.PieChart
import compose.icons.feathericons.Play
import compose.icons.feathericons.PlayCircle
import compose.icons.feathericons.Plus
import compose.icons.feathericons.PlusCircle
import compose.icons.feathericons.PlusSquare
import compose.icons.feathericons.Pocket
import compose.icons.feathericons.Power
import compose.icons.feathericons.Printer
import compose.icons.feathericons.Radio
import compose.icons.feathericons.RefreshCcw
import compose.icons.feathericons.RefreshCw
import compose.icons.feathericons.Repeat
import compose.icons.feathericons.Rewind
import compose.icons.feathericons.RotateCcw
import compose.icons.feathericons.RotateCw
import compose.icons.feathericons.Rss
import compose.icons.feathericons.Save
import compose.icons.feathericons.Scissors
import compose.icons.feathericons.Search
import compose.icons.feathericons.Send
import compose.icons.feathericons.Server
import compose.icons.feathericons.Settings
import compose.icons.feathericons.Share
import compose.icons.feathericons.Share2
import compose.icons.feathericons.Shield
import compose.icons.feathericons.ShieldOff
import compose.icons.feathericons.ShoppingBag
import compose.icons.feathericons.ShoppingCart
import compose.icons.feathericons.Shuffle
import compose.icons.feathericons.Sidebar
import compose.icons.feathericons.SkipBack
import compose.icons.feathericons.SkipForward
import compose.icons.feathericons.Slack
import compose.icons.feathericons.Slash
import compose.icons.feathericons.Sliders
import compose.icons.feathericons.Smartphone
import compose.icons.feathericons.Smile
import compose.icons.feathericons.Speaker
import compose.icons.feathericons.Square
import compose.icons.feathericons.Star
import compose.icons.feathericons.StopCircle
import compose.icons.feathericons.Sun
import compose.icons.feathericons.Sunrise
import compose.icons.feathericons.Sunset
import compose.icons.feathericons.Tablet
import compose.icons.feathericons.Tag
import compose.icons.feathericons.Target
import compose.icons.feathericons.Terminal
import compose.icons.feathericons.Thermometer
import compose.icons.feathericons.ThumbsDown
import compose.icons.feathericons.ThumbsUp
import compose.icons.feathericons.ToggleLeft
import compose.icons.feathericons.ToggleRight
import compose.icons.feathericons.Tool
import compose.icons.feathericons.Trash
import compose.icons.feathericons.Trash2
import compose.icons.feathericons.Trello
import compose.icons.feathericons.TrendingDown
import compose.icons.feathericons.TrendingUp
import compose.icons.feathericons.Triangle
import compose.icons.feathericons.Truck
import compose.icons.feathericons.Tv
import compose.icons.feathericons.Twitch
import compose.icons.feathericons.Twitter
import compose.icons.feathericons.Type
import compose.icons.feathericons.Umbrella
import compose.icons.feathericons.Underline
import compose.icons.feathericons.Unlock
import compose.icons.feathericons.Upload
import compose.icons.feathericons.UploadCloud
import compose.icons.feathericons.User
import compose.icons.feathericons.UserCheck
import compose.icons.feathericons.UserMinus
import compose.icons.feathericons.UserPlus
import compose.icons.feathericons.UserX
import compose.icons.feathericons.Users
import compose.icons.feathericons.Video
import compose.icons.feathericons.VideoOff
import compose.icons.feathericons.Voicemail
import compose.icons.feathericons.Volume
import compose.icons.feathericons.Volume1
import compose.icons.feathericons.Volume2
import compose.icons.feathericons.VolumeX
import compose.icons.feathericons.Watch
import compose.icons.feathericons.Wifi
import compose.icons.feathericons.WifiOff
import compose.icons.feathericons.Wind
import compose.icons.feathericons.X
import compose.icons.feathericons.XCircle
import compose.icons.feathericons.XOctagon
import compose.icons.feathericons.XSquare
import compose.icons.feathericons.Youtube
import compose.icons.feathericons.Zap
import compose.icons.feathericons.ZapOff
import compose.icons.feathericons.ZoomIn
import compose.icons.feathericons.ZoomOut
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.brands.AccessibleIcon
import compose.icons.fontawesomeicons.brands.Accusoft
import compose.icons.fontawesomeicons.brands.AcquisitionsIncorporated
import compose.icons.fontawesomeicons.brands.Adn
import compose.icons.fontawesomeicons.brands.Adversal
import compose.icons.fontawesomeicons.brands.Affiliatetheme
import compose.icons.fontawesomeicons.brands.Airbnb
import compose.icons.fontawesomeicons.brands.Algolia
import compose.icons.fontawesomeicons.brands.Alipay
import compose.icons.fontawesomeicons.brands.Amazon
import compose.icons.fontawesomeicons.brands.AmazonPay
import compose.icons.fontawesomeicons.brands.Amilia
import compose.icons.fontawesomeicons.brands.Android
import compose.icons.fontawesomeicons.brands.Angellist
import compose.icons.fontawesomeicons.brands.Angrycreative
import compose.icons.fontawesomeicons.brands.Angular
import compose.icons.fontawesomeicons.brands.AppStore
import compose.icons.fontawesomeicons.brands.AppStoreIos
import compose.icons.fontawesomeicons.brands.Apper
import compose.icons.fontawesomeicons.brands.Apple
import compose.icons.fontawesomeicons.brands.ApplePay
import compose.icons.fontawesomeicons.brands.Artstation
import compose.icons.fontawesomeicons.brands.Asymmetrik
import compose.icons.fontawesomeicons.brands.Atlassian
import compose.icons.fontawesomeicons.brands.Audible
import compose.icons.fontawesomeicons.brands.Autoprefixer
import compose.icons.fontawesomeicons.brands.Avianex
import compose.icons.fontawesomeicons.brands.Aviato
import compose.icons.fontawesomeicons.brands.Aws
import compose.icons.fontawesomeicons.brands.Bandcamp
import compose.icons.fontawesomeicons.brands.BattleNet
import compose.icons.fontawesomeicons.brands.Behance
import compose.icons.fontawesomeicons.brands.BehanceSquare
import compose.icons.fontawesomeicons.brands.Bimobject
import compose.icons.fontawesomeicons.brands.Bitbucket
import compose.icons.fontawesomeicons.brands.Bitcoin
import compose.icons.fontawesomeicons.brands.Bity
import compose.icons.fontawesomeicons.brands.BlackTie
import compose.icons.fontawesomeicons.brands.Blackberry
import compose.icons.fontawesomeicons.brands.Blogger
import compose.icons.fontawesomeicons.brands.BloggerB
import compose.icons.fontawesomeicons.brands.Bluetooth
import compose.icons.fontawesomeicons.brands.BluetoothB
import compose.icons.fontawesomeicons.brands.Bootstrap
import compose.icons.fontawesomeicons.brands.Btc
import compose.icons.fontawesomeicons.brands.Buffer
import compose.icons.fontawesomeicons.brands.Buromobelexperte
import compose.icons.fontawesomeicons.brands.BuyNLarge
import compose.icons.fontawesomeicons.brands.Buysellads
import compose.icons.fontawesomeicons.brands.CanadianMapleLeaf
import compose.icons.fontawesomeicons.brands.CcAmazonPay
import compose.icons.fontawesomeicons.brands.CcAmex
import compose.icons.fontawesomeicons.brands.CcApplePay
import compose.icons.fontawesomeicons.brands.CcDinersClub
import compose.icons.fontawesomeicons.brands.CcDiscover
import compose.icons.fontawesomeicons.brands.CcJcb
import compose.icons.fontawesomeicons.brands.CcMastercard
import compose.icons.fontawesomeicons.brands.CcPaypal
import compose.icons.fontawesomeicons.brands.CcStripe
import compose.icons.fontawesomeicons.brands.CcVisa
import compose.icons.fontawesomeicons.brands.Centercode
import compose.icons.fontawesomeicons.brands.Centos
import compose.icons.fontawesomeicons.brands.Chrome
import compose.icons.fontawesomeicons.brands.Chromecast
import compose.icons.fontawesomeicons.brands.Cloudflare
import compose.icons.fontawesomeicons.brands.Cloudscale
import compose.icons.fontawesomeicons.brands.Cloudsmith
import compose.icons.fontawesomeicons.brands.Cloudversify
import compose.icons.fontawesomeicons.brands.Codepen
import compose.icons.fontawesomeicons.brands.Codiepie
import compose.icons.fontawesomeicons.brands.Confluence
import compose.icons.fontawesomeicons.brands.Connectdevelop
import compose.icons.fontawesomeicons.brands.Contao
import compose.icons.fontawesomeicons.brands.CottonBureau
import compose.icons.fontawesomeicons.brands.Cpanel
import compose.icons.fontawesomeicons.brands.CreativeCommons
import compose.icons.fontawesomeicons.brands.CreativeCommonsBy
import compose.icons.fontawesomeicons.brands.CreativeCommonsNc
import compose.icons.fontawesomeicons.brands.CreativeCommonsNcEu
import compose.icons.fontawesomeicons.brands.CreativeCommonsNcJp
import compose.icons.fontawesomeicons.brands.CreativeCommonsNd
import compose.icons.fontawesomeicons.brands.CreativeCommonsPd
import compose.icons.fontawesomeicons.brands.CreativeCommonsPdAlt
import compose.icons.fontawesomeicons.brands.CreativeCommonsRemix
import compose.icons.fontawesomeicons.brands.CreativeCommonsSa
import compose.icons.fontawesomeicons.brands.CreativeCommonsSampling
import compose.icons.fontawesomeicons.brands.CreativeCommonsSamplingPlus
import compose.icons.fontawesomeicons.brands.CreativeCommonsShare
import compose.icons.fontawesomeicons.brands.CreativeCommonsZero
import compose.icons.fontawesomeicons.brands.CriticalRole
import compose.icons.fontawesomeicons.brands.Css3
import compose.icons.fontawesomeicons.brands.Css3Alt
import compose.icons.fontawesomeicons.brands.Cuttlefish
import compose.icons.fontawesomeicons.brands.DAndD
import compose.icons.fontawesomeicons.brands.DAndDBeyond
import compose.icons.fontawesomeicons.brands.Dailymotion
import compose.icons.fontawesomeicons.brands.Dashcube
import compose.icons.fontawesomeicons.brands.Deezer
import compose.icons.fontawesomeicons.brands.Delicious
import compose.icons.fontawesomeicons.brands.Deploydog
import compose.icons.fontawesomeicons.brands.Deskpro
import compose.icons.fontawesomeicons.brands.Dev
import compose.icons.fontawesomeicons.brands.Deviantart
import compose.icons.fontawesomeicons.brands.Dhl
import compose.icons.fontawesomeicons.brands.Diaspora
import compose.icons.fontawesomeicons.brands.Digg
import compose.icons.fontawesomeicons.brands.DigitalOcean
import compose.icons.fontawesomeicons.brands.Discord
import compose.icons.fontawesomeicons.brands.Discourse
import compose.icons.fontawesomeicons.brands.Dochub
import compose.icons.fontawesomeicons.brands.Docker
import compose.icons.fontawesomeicons.brands.Draft2digital
import compose.icons.fontawesomeicons.brands.Dribbble
import compose.icons.fontawesomeicons.brands.DribbbleSquare
import compose.icons.fontawesomeicons.brands.Dropbox
import compose.icons.fontawesomeicons.brands.Drupal
import compose.icons.fontawesomeicons.brands.Dyalog
import compose.icons.fontawesomeicons.brands.Earlybirds
import compose.icons.fontawesomeicons.brands.Ebay
import compose.icons.fontawesomeicons.brands.Edge
import compose.icons.fontawesomeicons.brands.EdgeLegacy
import compose.icons.fontawesomeicons.brands.Elementor
import compose.icons.fontawesomeicons.brands.Ello
import compose.icons.fontawesomeicons.brands.Ember
import compose.icons.fontawesomeicons.brands.Empire
import compose.icons.fontawesomeicons.brands.Envira
import compose.icons.fontawesomeicons.brands.Erlang
import compose.icons.fontawesomeicons.brands.Ethereum
import compose.icons.fontawesomeicons.brands.Etsy
import compose.icons.fontawesomeicons.brands.Evernote
import compose.icons.fontawesomeicons.brands.Expeditedssl
import compose.icons.fontawesomeicons.brands.Facebook
import compose.icons.fontawesomeicons.brands.FacebookF
import compose.icons.fontawesomeicons.brands.FacebookMessenger
import compose.icons.fontawesomeicons.brands.FacebookSquare
import compose.icons.fontawesomeicons.brands.FantasyFlightGames
import compose.icons.fontawesomeicons.brands.Fedex
import compose.icons.fontawesomeicons.brands.Fedora
import compose.icons.fontawesomeicons.brands.Figma
import compose.icons.fontawesomeicons.brands.Firefox
import compose.icons.fontawesomeicons.brands.FirefoxBrowser
import compose.icons.fontawesomeicons.brands.FirstOrder
import compose.icons.fontawesomeicons.brands.FirstOrderAlt
import compose.icons.fontawesomeicons.brands.Firstdraft
import compose.icons.fontawesomeicons.brands.Flickr
import compose.icons.fontawesomeicons.brands.Flipboard
import compose.icons.fontawesomeicons.brands.Fly
import compose.icons.fontawesomeicons.brands.FontAwesome
import compose.icons.fontawesomeicons.brands.FontAwesomeAlt
import compose.icons.fontawesomeicons.brands.FontAwesomeFlag
import compose.icons.fontawesomeicons.brands.FontAwesomeLogoFull
import compose.icons.fontawesomeicons.brands.Fonticons
import compose.icons.fontawesomeicons.brands.FonticonsFi
import compose.icons.fontawesomeicons.brands.FortAwesome
import compose.icons.fontawesomeicons.brands.FortAwesomeAlt
import compose.icons.fontawesomeicons.brands.Forumbee
import compose.icons.fontawesomeicons.brands.Foursquare
import compose.icons.fontawesomeicons.brands.FreeCodeCamp
import compose.icons.fontawesomeicons.brands.Freebsd
import compose.icons.fontawesomeicons.brands.Fulcrum
import compose.icons.fontawesomeicons.brands.GalacticRepublic
import compose.icons.fontawesomeicons.brands.GalacticSenate
import compose.icons.fontawesomeicons.brands.GetPocket
import compose.icons.fontawesomeicons.brands.Gg
import compose.icons.fontawesomeicons.brands.GgCircle
import compose.icons.fontawesomeicons.brands.Git
import compose.icons.fontawesomeicons.brands.GitAlt
import compose.icons.fontawesomeicons.brands.GitSquare
import compose.icons.fontawesomeicons.brands.Github
import compose.icons.fontawesomeicons.brands.GithubAlt
import compose.icons.fontawesomeicons.brands.GithubSquare
import compose.icons.fontawesomeicons.brands.Gitkraken
import compose.icons.fontawesomeicons.brands.Gitlab
import compose.icons.fontawesomeicons.brands.Gitter
import compose.icons.fontawesomeicons.brands.Glide
import compose.icons.fontawesomeicons.brands.GlideG
import compose.icons.fontawesomeicons.brands.Gofore
import compose.icons.fontawesomeicons.brands.Goodreads
import compose.icons.fontawesomeicons.brands.GoodreadsG
import compose.icons.fontawesomeicons.brands.Google
import compose.icons.fontawesomeicons.brands.GoogleDrive
import compose.icons.fontawesomeicons.brands.GooglePay
import compose.icons.fontawesomeicons.brands.GooglePlay
import compose.icons.fontawesomeicons.brands.GooglePlus
import compose.icons.fontawesomeicons.brands.GooglePlusG
import compose.icons.fontawesomeicons.brands.GooglePlusSquare
import compose.icons.fontawesomeicons.brands.GoogleWallet
import compose.icons.fontawesomeicons.brands.Gratipay
import compose.icons.fontawesomeicons.brands.Grav
import compose.icons.fontawesomeicons.brands.Gripfire
import compose.icons.fontawesomeicons.brands.Grunt
import compose.icons.fontawesomeicons.brands.Guilded
import compose.icons.fontawesomeicons.brands.Gulp
import compose.icons.fontawesomeicons.brands.HackerNews
import compose.icons.fontawesomeicons.brands.HackerNewsSquare
import compose.icons.fontawesomeicons.brands.Hackerrank
import compose.icons.fontawesomeicons.brands.Hips
import compose.icons.fontawesomeicons.brands.HireAHelper
import compose.icons.fontawesomeicons.brands.Hive
import compose.icons.fontawesomeicons.brands.Hooli
import compose.icons.fontawesomeicons.brands.Hornbill
import compose.icons.fontawesomeicons.brands.Hotjar
import compose.icons.fontawesomeicons.brands.Houzz
import compose.icons.fontawesomeicons.brands.Html5
import compose.icons.fontawesomeicons.brands.Hubspot
import compose.icons.fontawesomeicons.brands.Ideal
import compose.icons.fontawesomeicons.brands.Imdb
import compose.icons.fontawesomeicons.brands.Innosoft
import compose.icons.fontawesomeicons.brands.Instagram
import compose.icons.fontawesomeicons.brands.InstagramSquare
import compose.icons.fontawesomeicons.brands.Instalod
import compose.icons.fontawesomeicons.brands.Intercom
import compose.icons.fontawesomeicons.brands.InternetExplorer
import compose.icons.fontawesomeicons.brands.Invision
import compose.icons.fontawesomeicons.brands.Ioxhost
import compose.icons.fontawesomeicons.brands.ItchIo
import compose.icons.fontawesomeicons.brands.Itunes
import compose.icons.fontawesomeicons.brands.ItunesNote
import compose.icons.fontawesomeicons.brands.Java
import compose.icons.fontawesomeicons.brands.JediOrder
import compose.icons.fontawesomeicons.brands.Jenkins
import compose.icons.fontawesomeicons.brands.Jira
import compose.icons.fontawesomeicons.brands.Joget
import compose.icons.fontawesomeicons.brands.Joomla
import compose.icons.fontawesomeicons.brands.Js
import compose.icons.fontawesomeicons.brands.JsSquare
import compose.icons.fontawesomeicons.brands.Jsfiddle
import compose.icons.fontawesomeicons.brands.Kaggle
import compose.icons.fontawesomeicons.brands.Keybase
import compose.icons.fontawesomeicons.brands.Keycdn
import compose.icons.fontawesomeicons.brands.Kickstarter
import compose.icons.fontawesomeicons.brands.KickstarterK
import compose.icons.fontawesomeicons.brands.Korvue
import compose.icons.fontawesomeicons.brands.Laravel
import compose.icons.fontawesomeicons.brands.Lastfm
import compose.icons.fontawesomeicons.brands.LastfmSquare
import compose.icons.fontawesomeicons.brands.Leanpub
import compose.icons.fontawesomeicons.brands.Less
import compose.icons.fontawesomeicons.brands.Line
import compose.icons.fontawesomeicons.brands.Linkedin
import compose.icons.fontawesomeicons.brands.LinkedinIn
import compose.icons.fontawesomeicons.brands.Linode
import compose.icons.fontawesomeicons.brands.Linux
import compose.icons.fontawesomeicons.brands.Lyft
import compose.icons.fontawesomeicons.brands.Magento
import compose.icons.fontawesomeicons.brands.Mailchimp
import compose.icons.fontawesomeicons.brands.Mandalorian
import compose.icons.fontawesomeicons.brands.Markdown
import compose.icons.fontawesomeicons.brands.Mastodon
import compose.icons.fontawesomeicons.brands.Maxcdn
import compose.icons.fontawesomeicons.brands.Mdb
import compose.icons.fontawesomeicons.brands.Medapps
import compose.icons.fontawesomeicons.brands.Medium
import compose.icons.fontawesomeicons.brands.MediumM
import compose.icons.fontawesomeicons.brands.Medrt
import compose.icons.fontawesomeicons.brands.Meetup
import compose.icons.fontawesomeicons.brands.Megaport
import compose.icons.fontawesomeicons.brands.Mendeley
import compose.icons.fontawesomeicons.brands.Microblog
import compose.icons.fontawesomeicons.brands.Microsoft
import compose.icons.fontawesomeicons.brands.Mix
import compose.icons.fontawesomeicons.brands.Mixcloud
import compose.icons.fontawesomeicons.brands.Mixer
import compose.icons.fontawesomeicons.brands.Mizuni
import compose.icons.fontawesomeicons.brands.Modx
import compose.icons.fontawesomeicons.brands.Monero
import compose.icons.fontawesomeicons.brands.Napster
import compose.icons.fontawesomeicons.brands.Neos
import compose.icons.fontawesomeicons.brands.Nimblr
import compose.icons.fontawesomeicons.brands.Node
import compose.icons.fontawesomeicons.brands.NodeJs
import compose.icons.fontawesomeicons.brands.Npm
import compose.icons.fontawesomeicons.brands.Ns8
import compose.icons.fontawesomeicons.brands.Nutritionix
import compose.icons.fontawesomeicons.brands.OctopusDeploy
import compose.icons.fontawesomeicons.brands.Odnoklassniki
import compose.icons.fontawesomeicons.brands.OdnoklassnikiSquare
import compose.icons.fontawesomeicons.brands.OldRepublic
import compose.icons.fontawesomeicons.brands.Opencart
import compose.icons.fontawesomeicons.brands.Openid
import compose.icons.fontawesomeicons.brands.Opera
import compose.icons.fontawesomeicons.brands.OptinMonster
import compose.icons.fontawesomeicons.brands.Orcid
import compose.icons.fontawesomeicons.brands.Osi
import compose.icons.fontawesomeicons.brands.Page4
import compose.icons.fontawesomeicons.brands.Pagelines
import compose.icons.fontawesomeicons.brands.Palfed
import compose.icons.fontawesomeicons.brands.Patreon
import compose.icons.fontawesomeicons.brands.Paypal
import compose.icons.fontawesomeicons.brands.PennyArcade
import compose.icons.fontawesomeicons.brands.Perbyte
import compose.icons.fontawesomeicons.brands.Periscope
import compose.icons.fontawesomeicons.brands.Phabricator
import compose.icons.fontawesomeicons.brands.PhoenixFramework
import compose.icons.fontawesomeicons.brands.PhoenixSquadron
import compose.icons.fontawesomeicons.brands.Php
import compose.icons.fontawesomeicons.brands.PiedPiper
import compose.icons.fontawesomeicons.brands.PiedPiperAlt
import compose.icons.fontawesomeicons.brands.PiedPiperHat
import compose.icons.fontawesomeicons.brands.PiedPiperPp
import compose.icons.fontawesomeicons.brands.PiedPiperSquare
import compose.icons.fontawesomeicons.brands.Pinterest
import compose.icons.fontawesomeicons.brands.PinterestP
import compose.icons.fontawesomeicons.brands.PinterestSquare
import compose.icons.fontawesomeicons.brands.Playstation
import compose.icons.fontawesomeicons.brands.ProductHunt
import compose.icons.fontawesomeicons.brands.Pushed
import compose.icons.fontawesomeicons.brands.Python
import compose.icons.fontawesomeicons.brands.Qq
import compose.icons.fontawesomeicons.brands.Quinscape
import compose.icons.fontawesomeicons.brands.Quora
import compose.icons.fontawesomeicons.brands.RProject
import compose.icons.fontawesomeicons.brands.RaspberryPi
import compose.icons.fontawesomeicons.brands.Ravelry
import compose.icons.fontawesomeicons.brands.React
import compose.icons.fontawesomeicons.brands.Reacteurope
import compose.icons.fontawesomeicons.brands.Readme
import compose.icons.fontawesomeicons.brands.Rebel
import compose.icons.fontawesomeicons.brands.RedRiver
import compose.icons.fontawesomeicons.brands.Reddit
import compose.icons.fontawesomeicons.brands.RedditAlien
import compose.icons.fontawesomeicons.brands.RedditSquare
import compose.icons.fontawesomeicons.brands.Redhat
import compose.icons.fontawesomeicons.brands.Renren
import compose.icons.fontawesomeicons.brands.Replyd
import compose.icons.fontawesomeicons.brands.Researchgate
import compose.icons.fontawesomeicons.brands.Resolving
import compose.icons.fontawesomeicons.brands.Rev
import compose.icons.fontawesomeicons.brands.Rocketchat
import compose.icons.fontawesomeicons.brands.Rockrms
import compose.icons.fontawesomeicons.brands.Rust
import compose.icons.fontawesomeicons.brands.Safari
import compose.icons.fontawesomeicons.brands.Salesforce
import compose.icons.fontawesomeicons.brands.Sass
import compose.icons.fontawesomeicons.brands.Schlix
import compose.icons.fontawesomeicons.brands.Scribd
import compose.icons.fontawesomeicons.brands.Searchengin
import compose.icons.fontawesomeicons.brands.Sellcast
import compose.icons.fontawesomeicons.brands.Sellsy
import compose.icons.fontawesomeicons.brands.Servicestack
import compose.icons.fontawesomeicons.brands.Shirtsinbulk
import compose.icons.fontawesomeicons.brands.Shopify
import compose.icons.fontawesomeicons.brands.Shopware
import compose.icons.fontawesomeicons.brands.Simplybuilt
import compose.icons.fontawesomeicons.brands.Sistrix
import compose.icons.fontawesomeicons.brands.Sith
import compose.icons.fontawesomeicons.brands.Sketch
import compose.icons.fontawesomeicons.brands.Skyatlas
import compose.icons.fontawesomeicons.brands.Skype
import compose.icons.fontawesomeicons.brands.Slack
import compose.icons.fontawesomeicons.brands.SlackHash
import compose.icons.fontawesomeicons.brands.Slideshare
import compose.icons.fontawesomeicons.brands.Snapchat
import compose.icons.fontawesomeicons.brands.SnapchatGhost
import compose.icons.fontawesomeicons.brands.SnapchatSquare
import compose.icons.fontawesomeicons.brands.Soundcloud
import compose.icons.fontawesomeicons.brands.Sourcetree
import compose.icons.fontawesomeicons.brands.Speakap
import compose.icons.fontawesomeicons.brands.SpeakerDeck
import compose.icons.fontawesomeicons.brands.Spotify
import compose.icons.fontawesomeicons.brands.Squarespace
import compose.icons.fontawesomeicons.brands.StackExchange
import compose.icons.fontawesomeicons.brands.StackOverflow
import compose.icons.fontawesomeicons.brands.Stackpath
import compose.icons.fontawesomeicons.brands.Staylinked
import compose.icons.fontawesomeicons.brands.Steam
import compose.icons.fontawesomeicons.brands.SteamSquare
import compose.icons.fontawesomeicons.brands.SteamSymbol
import compose.icons.fontawesomeicons.brands.StickerMule
import compose.icons.fontawesomeicons.brands.Strava
import compose.icons.fontawesomeicons.brands.Stripe
import compose.icons.fontawesomeicons.brands.StripeS
import compose.icons.fontawesomeicons.brands.Studiovinari
import compose.icons.fontawesomeicons.brands.Stumbleupon
import compose.icons.fontawesomeicons.brands.StumbleuponCircle
import compose.icons.fontawesomeicons.brands.Superpowers
import compose.icons.fontawesomeicons.brands.Supple
import compose.icons.fontawesomeicons.brands.Suse
import compose.icons.fontawesomeicons.brands.Swift
import compose.icons.fontawesomeicons.brands.Symfony
import compose.icons.fontawesomeicons.brands.Teamspeak
import compose.icons.fontawesomeicons.brands.Telegram
import compose.icons.fontawesomeicons.brands.TelegramPlane
import compose.icons.fontawesomeicons.brands.TencentWeibo
import compose.icons.fontawesomeicons.brands.TheRedYeti
import compose.icons.fontawesomeicons.brands.Themeco
import compose.icons.fontawesomeicons.brands.Themeisle
import compose.icons.fontawesomeicons.brands.ThinkPeaks
import compose.icons.fontawesomeicons.brands.Tiktok
import compose.icons.fontawesomeicons.brands.TradeFederation
import compose.icons.fontawesomeicons.brands.Trello
import compose.icons.fontawesomeicons.brands.Tripadvisor
import compose.icons.fontawesomeicons.brands.Tumblr
import compose.icons.fontawesomeicons.brands.TumblrSquare
import compose.icons.fontawesomeicons.brands.Twitch
import compose.icons.fontawesomeicons.brands.Twitter
import compose.icons.fontawesomeicons.brands.TwitterSquare
import compose.icons.fontawesomeicons.brands.Typo3
import compose.icons.fontawesomeicons.brands.Uber
import compose.icons.fontawesomeicons.brands.Ubuntu
import compose.icons.fontawesomeicons.brands.Uikit
import compose.icons.fontawesomeicons.brands.Umbraco
import compose.icons.fontawesomeicons.brands.Uncharted
import compose.icons.fontawesomeicons.brands.Uniregistry
import compose.icons.fontawesomeicons.brands.Unity
import compose.icons.fontawesomeicons.brands.Unsplash
import compose.icons.fontawesomeicons.brands.Untappd
import compose.icons.fontawesomeicons.brands.Ups
import compose.icons.fontawesomeicons.brands.Usb
import compose.icons.fontawesomeicons.brands.Usps
import compose.icons.fontawesomeicons.brands.Ussunnah
import compose.icons.fontawesomeicons.brands.Vaadin
import compose.icons.fontawesomeicons.brands.Viacoin
import compose.icons.fontawesomeicons.brands.Viadeo
import compose.icons.fontawesomeicons.brands.ViadeoSquare
import compose.icons.fontawesomeicons.brands.Viber
import compose.icons.fontawesomeicons.brands.Vimeo
import compose.icons.fontawesomeicons.brands.VimeoSquare
import compose.icons.fontawesomeicons.brands.VimeoV
import compose.icons.fontawesomeicons.brands.Vine
import compose.icons.fontawesomeicons.brands.Vk
import compose.icons.fontawesomeicons.brands.Vnv
import compose.icons.fontawesomeicons.brands.Vuejs
import compose.icons.fontawesomeicons.brands.WatchmanMonitoring
import compose.icons.fontawesomeicons.brands.Waze
import compose.icons.fontawesomeicons.brands.Weebly
import compose.icons.fontawesomeicons.brands.Weibo
import compose.icons.fontawesomeicons.brands.Weixin
import compose.icons.fontawesomeicons.brands.Whatsapp
import compose.icons.fontawesomeicons.brands.WhatsappSquare
import compose.icons.fontawesomeicons.brands.Whmcs
import compose.icons.fontawesomeicons.brands.WikipediaW
import compose.icons.fontawesomeicons.brands.Windows
import compose.icons.fontawesomeicons.brands.Wix
import compose.icons.fontawesomeicons.brands.WizardsOfTheCoast
import compose.icons.fontawesomeicons.brands.Wodu
import compose.icons.fontawesomeicons.brands.WolfPackBattalion
import compose.icons.fontawesomeicons.brands.Wordpress
import compose.icons.fontawesomeicons.brands.WordpressSimple
import compose.icons.fontawesomeicons.brands.Wpbeginner
import compose.icons.fontawesomeicons.brands.Wpexplorer
import compose.icons.fontawesomeicons.brands.Wpforms
import compose.icons.fontawesomeicons.brands.Wpressr
import compose.icons.fontawesomeicons.brands.Xbox
import compose.icons.fontawesomeicons.brands.Xing
import compose.icons.fontawesomeicons.brands.XingSquare
import compose.icons.fontawesomeicons.brands.YCombinator
import compose.icons.fontawesomeicons.brands.Yahoo
import compose.icons.fontawesomeicons.brands.Yammer
import compose.icons.fontawesomeicons.brands.Yandex
import compose.icons.fontawesomeicons.brands.YandexInternational
import compose.icons.fontawesomeicons.brands.Yarn
import compose.icons.fontawesomeicons.brands.Yelp
import compose.icons.fontawesomeicons.brands.Yoast
import compose.icons.fontawesomeicons.brands.Youtube
import compose.icons.fontawesomeicons.brands.YoutubeSquare
import compose.icons.fontawesomeicons.brands.Zhihu
import compose.icons.fontawesomeicons.brands._500px
import compose.icons.fontawesomeicons.regular.AddressBook
import compose.icons.fontawesomeicons.regular.AddressCard
import compose.icons.fontawesomeicons.regular.Angry
import compose.icons.fontawesomeicons.regular.ArrowAltCircleDown
import compose.icons.fontawesomeicons.regular.ArrowAltCircleLeft
import compose.icons.fontawesomeicons.regular.ArrowAltCircleRight
import compose.icons.fontawesomeicons.regular.ArrowAltCircleUp
import compose.icons.fontawesomeicons.regular.Bell
import compose.icons.fontawesomeicons.regular.BellSlash
import compose.icons.fontawesomeicons.regular.Bookmark
import compose.icons.fontawesomeicons.regular.Building
import compose.icons.fontawesomeicons.regular.Calendar
import compose.icons.fontawesomeicons.regular.CalendarAlt
import compose.icons.fontawesomeicons.regular.CalendarCheck
import compose.icons.fontawesomeicons.regular.CalendarMinus
import compose.icons.fontawesomeicons.regular.CalendarPlus
import compose.icons.fontawesomeicons.regular.CalendarTimes
import compose.icons.fontawesomeicons.regular.CaretSquareDown
import compose.icons.fontawesomeicons.regular.CaretSquareLeft
import compose.icons.fontawesomeicons.regular.CaretSquareRight
import compose.icons.fontawesomeicons.regular.CaretSquareUp
import compose.icons.fontawesomeicons.regular.ChartBar
import compose.icons.fontawesomeicons.regular.CheckCircle
import compose.icons.fontawesomeicons.regular.CheckSquare
import compose.icons.fontawesomeicons.regular.Circle
import compose.icons.fontawesomeicons.regular.Clipboard
import compose.icons.fontawesomeicons.regular.Clock
import compose.icons.fontawesomeicons.regular.Clone
import compose.icons.fontawesomeicons.regular.ClosedCaptioning
import compose.icons.fontawesomeicons.regular.Comment
import compose.icons.fontawesomeicons.regular.CommentAlt
import compose.icons.fontawesomeicons.regular.CommentDots
import compose.icons.fontawesomeicons.regular.Comments
import compose.icons.fontawesomeicons.regular.Compass
import compose.icons.fontawesomeicons.regular.Copy
import compose.icons.fontawesomeicons.regular.Copyright
import compose.icons.fontawesomeicons.regular.CreditCard
import compose.icons.fontawesomeicons.regular.Dizzy
import compose.icons.fontawesomeicons.regular.DotCircle
import compose.icons.fontawesomeicons.regular.Edit
import compose.icons.fontawesomeicons.regular.Envelope
import compose.icons.fontawesomeicons.regular.EnvelopeOpen
import compose.icons.fontawesomeicons.regular.Eye
import compose.icons.fontawesomeicons.regular.EyeSlash
import compose.icons.fontawesomeicons.regular.File
import compose.icons.fontawesomeicons.regular.FileAlt
import compose.icons.fontawesomeicons.regular.FileArchive
import compose.icons.fontawesomeicons.regular.FileAudio
import compose.icons.fontawesomeicons.regular.FileCode
import compose.icons.fontawesomeicons.regular.FileExcel
import compose.icons.fontawesomeicons.regular.FileImage
import compose.icons.fontawesomeicons.regular.FilePdf
import compose.icons.fontawesomeicons.regular.FilePowerpoint
import compose.icons.fontawesomeicons.regular.FileVideo
import compose.icons.fontawesomeicons.regular.FileWord
import compose.icons.fontawesomeicons.regular.Flag
import compose.icons.fontawesomeicons.regular.Flushed
import compose.icons.fontawesomeicons.regular.Folder
import compose.icons.fontawesomeicons.regular.FolderOpen
import compose.icons.fontawesomeicons.regular.FontAwesomeLogoFull
import compose.icons.fontawesomeicons.regular.Frown
import compose.icons.fontawesomeicons.regular.FrownOpen
import compose.icons.fontawesomeicons.regular.Futbol
import compose.icons.fontawesomeicons.regular.Gem
import compose.icons.fontawesomeicons.regular.Grimace
import compose.icons.fontawesomeicons.regular.Grin
import compose.icons.fontawesomeicons.regular.GrinAlt
import compose.icons.fontawesomeicons.regular.GrinBeam
import compose.icons.fontawesomeicons.regular.GrinBeamSweat
import compose.icons.fontawesomeicons.regular.GrinHearts
import compose.icons.fontawesomeicons.regular.GrinSquint
import compose.icons.fontawesomeicons.regular.GrinSquintTears
import compose.icons.fontawesomeicons.regular.GrinStars
import compose.icons.fontawesomeicons.regular.GrinTears
import compose.icons.fontawesomeicons.regular.GrinTongue
import compose.icons.fontawesomeicons.regular.GrinTongueSquint
import compose.icons.fontawesomeicons.regular.GrinTongueWink
import compose.icons.fontawesomeicons.regular.GrinWink
import compose.icons.fontawesomeicons.regular.HandLizard
import compose.icons.fontawesomeicons.regular.HandPaper
import compose.icons.fontawesomeicons.regular.HandPeace
import compose.icons.fontawesomeicons.regular.HandPointDown
import compose.icons.fontawesomeicons.regular.HandPointLeft
import compose.icons.fontawesomeicons.regular.HandPointRight
import compose.icons.fontawesomeicons.regular.HandPointUp
import compose.icons.fontawesomeicons.regular.HandPointer
import compose.icons.fontawesomeicons.regular.HandRock
import compose.icons.fontawesomeicons.regular.HandScissors
import compose.icons.fontawesomeicons.regular.HandSpock
import compose.icons.fontawesomeicons.regular.Handshake
import compose.icons.fontawesomeicons.regular.Hdd
import compose.icons.fontawesomeicons.regular.Heart
import compose.icons.fontawesomeicons.regular.Hospital
import compose.icons.fontawesomeicons.regular.Hourglass
import compose.icons.fontawesomeicons.regular.IdBadge
import compose.icons.fontawesomeicons.regular.IdCard
import compose.icons.fontawesomeicons.regular.Image
import compose.icons.fontawesomeicons.regular.Images
import compose.icons.fontawesomeicons.regular.Keyboard
import compose.icons.fontawesomeicons.regular.Kiss
import compose.icons.fontawesomeicons.regular.KissBeam
import compose.icons.fontawesomeicons.regular.KissWinkHeart
import compose.icons.fontawesomeicons.regular.Laugh
import compose.icons.fontawesomeicons.regular.LaughBeam
import compose.icons.fontawesomeicons.regular.LaughSquint
import compose.icons.fontawesomeicons.regular.LaughWink
import compose.icons.fontawesomeicons.regular.Lemon
import compose.icons.fontawesomeicons.regular.LifeRing
import compose.icons.fontawesomeicons.regular.Lightbulb
import compose.icons.fontawesomeicons.regular.ListAlt
import compose.icons.fontawesomeicons.regular.Map
import compose.icons.fontawesomeicons.regular.Meh
import compose.icons.fontawesomeicons.regular.MehBlank
import compose.icons.fontawesomeicons.regular.MehRollingEyes
import compose.icons.fontawesomeicons.regular.MinusSquare
import compose.icons.fontawesomeicons.regular.MoneyBillAlt
import compose.icons.fontawesomeicons.regular.Moon
import compose.icons.fontawesomeicons.regular.Newspaper
import compose.icons.fontawesomeicons.regular.ObjectGroup
import compose.icons.fontawesomeicons.regular.ObjectUngroup
import compose.icons.fontawesomeicons.regular.PaperPlane
import compose.icons.fontawesomeicons.regular.PauseCircle
import compose.icons.fontawesomeicons.regular.PlayCircle
import compose.icons.fontawesomeicons.regular.PlusSquare
import compose.icons.fontawesomeicons.regular.QuestionCircle
import compose.icons.fontawesomeicons.regular.Registered
import compose.icons.fontawesomeicons.regular.SadCry
import compose.icons.fontawesomeicons.regular.SadTear
import compose.icons.fontawesomeicons.regular.Save
import compose.icons.fontawesomeicons.regular.ShareSquare
import compose.icons.fontawesomeicons.regular.Smile
import compose.icons.fontawesomeicons.regular.SmileBeam
import compose.icons.fontawesomeicons.regular.SmileWink
import compose.icons.fontawesomeicons.regular.Snowflake
import compose.icons.fontawesomeicons.regular.Square
import compose.icons.fontawesomeicons.regular.Star
import compose.icons.fontawesomeicons.regular.StarHalf
import compose.icons.fontawesomeicons.regular.StickyNote
import compose.icons.fontawesomeicons.regular.StopCircle
import compose.icons.fontawesomeicons.regular.Sun
import compose.icons.fontawesomeicons.regular.Surprise
import compose.icons.fontawesomeicons.regular.ThumbsDown
import compose.icons.fontawesomeicons.regular.ThumbsUp
import compose.icons.fontawesomeicons.regular.TimesCircle
import compose.icons.fontawesomeicons.regular.Tired
import compose.icons.fontawesomeicons.regular.TrashAlt
import compose.icons.fontawesomeicons.regular.User
import compose.icons.fontawesomeicons.regular.UserCircle
import compose.icons.fontawesomeicons.regular.WindowClose
import compose.icons.fontawesomeicons.regular.WindowMaximize
import compose.icons.fontawesomeicons.regular.WindowMinimize
import compose.icons.fontawesomeicons.regular.WindowRestore
import compose.icons.fontawesomeicons.solid.Ad
import compose.icons.fontawesomeicons.solid.AddressBook
import compose.icons.fontawesomeicons.solid.AddressCard
import compose.icons.fontawesomeicons.solid.Adjust
import compose.icons.fontawesomeicons.solid.AirFreshener
import compose.icons.fontawesomeicons.solid.AlignCenter
import compose.icons.fontawesomeicons.solid.AlignJustify
import compose.icons.fontawesomeicons.solid.AlignLeft
import compose.icons.fontawesomeicons.solid.AlignRight
import compose.icons.fontawesomeicons.solid.Allergies
import compose.icons.fontawesomeicons.solid.Ambulance
import compose.icons.fontawesomeicons.solid.AmericanSignLanguageInterpreting
import compose.icons.fontawesomeicons.solid.Anchor
import compose.icons.fontawesomeicons.solid.AngleDoubleDown
import compose.icons.fontawesomeicons.solid.AngleDoubleLeft
import compose.icons.fontawesomeicons.solid.AngleDoubleRight
import compose.icons.fontawesomeicons.solid.AngleDoubleUp
import compose.icons.fontawesomeicons.solid.AngleDown
import compose.icons.fontawesomeicons.solid.AngleLeft
import compose.icons.fontawesomeicons.solid.AngleRight
import compose.icons.fontawesomeicons.solid.AngleUp
import compose.icons.fontawesomeicons.solid.Angry
import compose.icons.fontawesomeicons.solid.Ankh
import compose.icons.fontawesomeicons.solid.AppleAlt
import compose.icons.fontawesomeicons.solid.Archive
import compose.icons.fontawesomeicons.solid.Archway
import compose.icons.fontawesomeicons.solid.ArrowAltCircleDown
import compose.icons.fontawesomeicons.solid.ArrowAltCircleLeft
import compose.icons.fontawesomeicons.solid.ArrowAltCircleRight
import compose.icons.fontawesomeicons.solid.ArrowAltCircleUp
import compose.icons.fontawesomeicons.solid.ArrowCircleDown
import compose.icons.fontawesomeicons.solid.ArrowCircleLeft
import compose.icons.fontawesomeicons.solid.ArrowCircleRight
import compose.icons.fontawesomeicons.solid.ArrowCircleUp
import compose.icons.fontawesomeicons.solid.ArrowDown
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.ArrowRight
import compose.icons.fontawesomeicons.solid.ArrowUp
import compose.icons.fontawesomeicons.solid.ArrowsAlt
import compose.icons.fontawesomeicons.solid.ArrowsAltH
import compose.icons.fontawesomeicons.solid.ArrowsAltV
import compose.icons.fontawesomeicons.solid.AssistiveListeningSystems
import compose.icons.fontawesomeicons.solid.Asterisk
import compose.icons.fontawesomeicons.solid.At
import compose.icons.fontawesomeicons.solid.Atlas
import compose.icons.fontawesomeicons.solid.Atom
import compose.icons.fontawesomeicons.solid.AudioDescription
import compose.icons.fontawesomeicons.solid.Award
import compose.icons.fontawesomeicons.solid.Baby
import compose.icons.fontawesomeicons.solid.BabyCarriage
import compose.icons.fontawesomeicons.solid.Backspace
import compose.icons.fontawesomeicons.solid.Backward
import compose.icons.fontawesomeicons.solid.Bacon
import compose.icons.fontawesomeicons.solid.Bacteria
import compose.icons.fontawesomeicons.solid.Bacterium
import compose.icons.fontawesomeicons.solid.Bahai
import compose.icons.fontawesomeicons.solid.BalanceScale
import compose.icons.fontawesomeicons.solid.BalanceScaleLeft
import compose.icons.fontawesomeicons.solid.BalanceScaleRight
import compose.icons.fontawesomeicons.solid.Ban
import compose.icons.fontawesomeicons.solid.BandAid
import compose.icons.fontawesomeicons.solid.Barcode
import compose.icons.fontawesomeicons.solid.Bars
import compose.icons.fontawesomeicons.solid.BaseballBall
import compose.icons.fontawesomeicons.solid.BasketballBall
import compose.icons.fontawesomeicons.solid.Bath
import compose.icons.fontawesomeicons.solid.BatteryEmpty
import compose.icons.fontawesomeicons.solid.BatteryFull
import compose.icons.fontawesomeicons.solid.BatteryHalf
import compose.icons.fontawesomeicons.solid.BatteryQuarter
import compose.icons.fontawesomeicons.solid.BatteryThreeQuarters
import compose.icons.fontawesomeicons.solid.Bed
import compose.icons.fontawesomeicons.solid.Beer
import compose.icons.fontawesomeicons.solid.Bell
import compose.icons.fontawesomeicons.solid.BellSlash
import compose.icons.fontawesomeicons.solid.BezierCurve
import compose.icons.fontawesomeicons.solid.Bible
import compose.icons.fontawesomeicons.solid.Bicycle
import compose.icons.fontawesomeicons.solid.Biking
import compose.icons.fontawesomeicons.solid.Binoculars
import compose.icons.fontawesomeicons.solid.Biohazard
import compose.icons.fontawesomeicons.solid.BirthdayCake
import compose.icons.fontawesomeicons.solid.Blender
import compose.icons.fontawesomeicons.solid.BlenderPhone
import compose.icons.fontawesomeicons.solid.Blind
import compose.icons.fontawesomeicons.solid.Blog
import compose.icons.fontawesomeicons.solid.Bold
import compose.icons.fontawesomeicons.solid.Bolt
import compose.icons.fontawesomeicons.solid.Bomb
import compose.icons.fontawesomeicons.solid.Bone
import compose.icons.fontawesomeicons.solid.Bong
import compose.icons.fontawesomeicons.solid.Book
import compose.icons.fontawesomeicons.solid.BookDead
import compose.icons.fontawesomeicons.solid.BookMedical
import compose.icons.fontawesomeicons.solid.BookOpen
import compose.icons.fontawesomeicons.solid.BookReader
import compose.icons.fontawesomeicons.solid.Bookmark
import compose.icons.fontawesomeicons.solid.BorderAll
import compose.icons.fontawesomeicons.solid.BorderNone
import compose.icons.fontawesomeicons.solid.BorderStyle
import compose.icons.fontawesomeicons.solid.BowlingBall
import compose.icons.fontawesomeicons.solid.Box
import compose.icons.fontawesomeicons.solid.BoxOpen
import compose.icons.fontawesomeicons.solid.BoxTissue
import compose.icons.fontawesomeicons.solid.Boxes
import compose.icons.fontawesomeicons.solid.Braille
import compose.icons.fontawesomeicons.solid.Brain
import compose.icons.fontawesomeicons.solid.BreadSlice
import compose.icons.fontawesomeicons.solid.Briefcase
import compose.icons.fontawesomeicons.solid.BriefcaseMedical
import compose.icons.fontawesomeicons.solid.BroadcastTower
import compose.icons.fontawesomeicons.solid.Broom
import compose.icons.fontawesomeicons.solid.Brush
import compose.icons.fontawesomeicons.solid.Bug
import compose.icons.fontawesomeicons.solid.Building
import compose.icons.fontawesomeicons.solid.Bullhorn
import compose.icons.fontawesomeicons.solid.Bullseye
import compose.icons.fontawesomeicons.solid.Burn
import compose.icons.fontawesomeicons.solid.Bus
import compose.icons.fontawesomeicons.solid.BusAlt
import compose.icons.fontawesomeicons.solid.BusinessTime
import compose.icons.fontawesomeicons.solid.Calculator
import compose.icons.fontawesomeicons.solid.Calendar
import compose.icons.fontawesomeicons.solid.CalendarAlt
import compose.icons.fontawesomeicons.solid.CalendarCheck
import compose.icons.fontawesomeicons.solid.CalendarDay
import compose.icons.fontawesomeicons.solid.CalendarMinus
import compose.icons.fontawesomeicons.solid.CalendarPlus
import compose.icons.fontawesomeicons.solid.CalendarTimes
import compose.icons.fontawesomeicons.solid.CalendarWeek
import compose.icons.fontawesomeicons.solid.Camera
import compose.icons.fontawesomeicons.solid.CameraRetro
import compose.icons.fontawesomeicons.solid.Campground
import compose.icons.fontawesomeicons.solid.CandyCane
import compose.icons.fontawesomeicons.solid.Cannabis
import compose.icons.fontawesomeicons.solid.Capsules
import compose.icons.fontawesomeicons.solid.Car
import compose.icons.fontawesomeicons.solid.CarAlt
import compose.icons.fontawesomeicons.solid.CarBattery
import compose.icons.fontawesomeicons.solid.CarCrash
import compose.icons.fontawesomeicons.solid.CarSide
import compose.icons.fontawesomeicons.solid.Caravan
import compose.icons.fontawesomeicons.solid.CaretDown
import compose.icons.fontawesomeicons.solid.CaretLeft
import compose.icons.fontawesomeicons.solid.CaretRight
import compose.icons.fontawesomeicons.solid.CaretSquareDown
import compose.icons.fontawesomeicons.solid.CaretSquareLeft
import compose.icons.fontawesomeicons.solid.CaretSquareRight
import compose.icons.fontawesomeicons.solid.CaretSquareUp
import compose.icons.fontawesomeicons.solid.CaretUp
import compose.icons.fontawesomeicons.solid.Carrot
import compose.icons.fontawesomeicons.solid.CartArrowDown
import compose.icons.fontawesomeicons.solid.CartPlus
import compose.icons.fontawesomeicons.solid.CashRegister
import compose.icons.fontawesomeicons.solid.Cat
import compose.icons.fontawesomeicons.solid.Certificate
import compose.icons.fontawesomeicons.solid.Chair
import compose.icons.fontawesomeicons.solid.Chalkboard
import compose.icons.fontawesomeicons.solid.ChalkboardTeacher
import compose.icons.fontawesomeicons.solid.ChargingStation
import compose.icons.fontawesomeicons.solid.ChartArea
import compose.icons.fontawesomeicons.solid.ChartBar
import compose.icons.fontawesomeicons.solid.ChartLine
import compose.icons.fontawesomeicons.solid.ChartPie
import compose.icons.fontawesomeicons.solid.Check
import compose.icons.fontawesomeicons.solid.CheckCircle
import compose.icons.fontawesomeicons.solid.CheckDouble
import compose.icons.fontawesomeicons.solid.CheckSquare
import compose.icons.fontawesomeicons.solid.Cheese
import compose.icons.fontawesomeicons.solid.Chess
import compose.icons.fontawesomeicons.solid.ChessBishop
import compose.icons.fontawesomeicons.solid.ChessBoard
import compose.icons.fontawesomeicons.solid.ChessKing
import compose.icons.fontawesomeicons.solid.ChessKnight
import compose.icons.fontawesomeicons.solid.ChessPawn
import compose.icons.fontawesomeicons.solid.ChessQueen
import compose.icons.fontawesomeicons.solid.ChessRook
import compose.icons.fontawesomeicons.solid.ChevronCircleDown
import compose.icons.fontawesomeicons.solid.ChevronCircleLeft
import compose.icons.fontawesomeicons.solid.ChevronCircleRight
import compose.icons.fontawesomeicons.solid.ChevronCircleUp
import compose.icons.fontawesomeicons.solid.ChevronDown
import compose.icons.fontawesomeicons.solid.ChevronLeft
import compose.icons.fontawesomeicons.solid.ChevronRight
import compose.icons.fontawesomeicons.solid.ChevronUp
import compose.icons.fontawesomeicons.solid.Child
import compose.icons.fontawesomeicons.solid.Church
import compose.icons.fontawesomeicons.solid.Circle
import compose.icons.fontawesomeicons.solid.CircleNotch
import compose.icons.fontawesomeicons.solid.City
import compose.icons.fontawesomeicons.solid.ClinicMedical
import compose.icons.fontawesomeicons.solid.Clipboard
import compose.icons.fontawesomeicons.solid.ClipboardCheck
import compose.icons.fontawesomeicons.solid.ClipboardList
import compose.icons.fontawesomeicons.solid.Clock
import compose.icons.fontawesomeicons.solid.Clone
import compose.icons.fontawesomeicons.solid.ClosedCaptioning
import compose.icons.fontawesomeicons.solid.Cloud
import compose.icons.fontawesomeicons.solid.CloudDownloadAlt
import compose.icons.fontawesomeicons.solid.CloudMeatball
import compose.icons.fontawesomeicons.solid.CloudMoon
import compose.icons.fontawesomeicons.solid.CloudMoonRain
import compose.icons.fontawesomeicons.solid.CloudRain
import compose.icons.fontawesomeicons.solid.CloudShowersHeavy
import compose.icons.fontawesomeicons.solid.CloudSun
import compose.icons.fontawesomeicons.solid.CloudSunRain
import compose.icons.fontawesomeicons.solid.CloudUploadAlt
import compose.icons.fontawesomeicons.solid.Cocktail
import compose.icons.fontawesomeicons.solid.Code
import compose.icons.fontawesomeicons.solid.CodeBranch
import compose.icons.fontawesomeicons.solid.Coffee
import compose.icons.fontawesomeicons.solid.Cog
import compose.icons.fontawesomeicons.solid.Cogs
import compose.icons.fontawesomeicons.solid.Coins
import compose.icons.fontawesomeicons.solid.Columns
import compose.icons.fontawesomeicons.solid.Comment
import compose.icons.fontawesomeicons.solid.CommentAlt
import compose.icons.fontawesomeicons.solid.CommentDollar
import compose.icons.fontawesomeicons.solid.CommentDots
import compose.icons.fontawesomeicons.solid.CommentMedical
import compose.icons.fontawesomeicons.solid.CommentSlash
import compose.icons.fontawesomeicons.solid.Comments
import compose.icons.fontawesomeicons.solid.CommentsDollar
import compose.icons.fontawesomeicons.solid.CompactDisc
import compose.icons.fontawesomeicons.solid.Compass
import compose.icons.fontawesomeicons.solid.Compress
import compose.icons.fontawesomeicons.solid.CompressAlt
import compose.icons.fontawesomeicons.solid.CompressArrowsAlt
import compose.icons.fontawesomeicons.solid.ConciergeBell
import compose.icons.fontawesomeicons.solid.Cookie
import compose.icons.fontawesomeicons.solid.CookieBite
import compose.icons.fontawesomeicons.solid.Copy
import compose.icons.fontawesomeicons.solid.Copyright
import compose.icons.fontawesomeicons.solid.Couch
import compose.icons.fontawesomeicons.solid.CreditCard
import compose.icons.fontawesomeicons.solid.Crop
import compose.icons.fontawesomeicons.solid.CropAlt
import compose.icons.fontawesomeicons.solid.Cross
import compose.icons.fontawesomeicons.solid.Crosshairs
import compose.icons.fontawesomeicons.solid.Crow
import compose.icons.fontawesomeicons.solid.Crown
import compose.icons.fontawesomeicons.solid.Crutch
import compose.icons.fontawesomeicons.solid.Cube
import compose.icons.fontawesomeicons.solid.Cubes
import compose.icons.fontawesomeicons.solid.Cut
import compose.icons.fontawesomeicons.solid.Database
import compose.icons.fontawesomeicons.solid.Deaf
import compose.icons.fontawesomeicons.solid.Democrat
import compose.icons.fontawesomeicons.solid.Desktop
import compose.icons.fontawesomeicons.solid.Dharmachakra
import compose.icons.fontawesomeicons.solid.Diagnoses
import compose.icons.fontawesomeicons.solid.Dice
import compose.icons.fontawesomeicons.solid.DiceD20
import compose.icons.fontawesomeicons.solid.DiceD6
import compose.icons.fontawesomeicons.solid.DiceFive
import compose.icons.fontawesomeicons.solid.DiceFour
import compose.icons.fontawesomeicons.solid.DiceOne
import compose.icons.fontawesomeicons.solid.DiceSix
import compose.icons.fontawesomeicons.solid.DiceThree
import compose.icons.fontawesomeicons.solid.DiceTwo
import compose.icons.fontawesomeicons.solid.DigitalTachograph
import compose.icons.fontawesomeicons.solid.Directions
import compose.icons.fontawesomeicons.solid.Disease
import compose.icons.fontawesomeicons.solid.Divide
import compose.icons.fontawesomeicons.solid.Dizzy
import compose.icons.fontawesomeicons.solid.Dna
import compose.icons.fontawesomeicons.solid.Dog
import compose.icons.fontawesomeicons.solid.DollarSign
import compose.icons.fontawesomeicons.solid.Dolly
import compose.icons.fontawesomeicons.solid.DollyFlatbed
import compose.icons.fontawesomeicons.solid.Donate
import compose.icons.fontawesomeicons.solid.DoorClosed
import compose.icons.fontawesomeicons.solid.DoorOpen
import compose.icons.fontawesomeicons.solid.DotCircle
import compose.icons.fontawesomeicons.solid.Dove
import compose.icons.fontawesomeicons.solid.Download
import compose.icons.fontawesomeicons.solid.DraftingCompass
import compose.icons.fontawesomeicons.solid.Dragon
import compose.icons.fontawesomeicons.solid.DrawPolygon
import compose.icons.fontawesomeicons.solid.Drum
import compose.icons.fontawesomeicons.solid.DrumSteelpan
import compose.icons.fontawesomeicons.solid.DrumstickBite
import compose.icons.fontawesomeicons.solid.Dumbbell
import compose.icons.fontawesomeicons.solid.Dumpster
import compose.icons.fontawesomeicons.solid.DumpsterFire
import compose.icons.fontawesomeicons.solid.Dungeon
import compose.icons.fontawesomeicons.solid.Edit
import compose.icons.fontawesomeicons.solid.Egg
import compose.icons.fontawesomeicons.solid.Eject
import compose.icons.fontawesomeicons.solid.EllipsisH
import compose.icons.fontawesomeicons.solid.EllipsisV
import compose.icons.fontawesomeicons.solid.Envelope
import compose.icons.fontawesomeicons.solid.EnvelopeOpen
import compose.icons.fontawesomeicons.solid.EnvelopeOpenText
import compose.icons.fontawesomeicons.solid.EnvelopeSquare
import compose.icons.fontawesomeicons.solid.Equals
import compose.icons.fontawesomeicons.solid.Eraser
import compose.icons.fontawesomeicons.solid.Ethernet
import compose.icons.fontawesomeicons.solid.EuroSign
import compose.icons.fontawesomeicons.solid.ExchangeAlt
import compose.icons.fontawesomeicons.solid.Exclamation
import compose.icons.fontawesomeicons.solid.ExclamationCircle
import compose.icons.fontawesomeicons.solid.ExclamationTriangle
import compose.icons.fontawesomeicons.solid.Expand
import compose.icons.fontawesomeicons.solid.ExpandAlt
import compose.icons.fontawesomeicons.solid.ExpandArrowsAlt
import compose.icons.fontawesomeicons.solid.ExternalLinkAlt
import compose.icons.fontawesomeicons.solid.ExternalLinkSquareAlt
import compose.icons.fontawesomeicons.solid.Eye
import compose.icons.fontawesomeicons.solid.EyeDropper
import compose.icons.fontawesomeicons.solid.EyeSlash
import compose.icons.fontawesomeicons.solid.Fan
import compose.icons.fontawesomeicons.solid.FastBackward
import compose.icons.fontawesomeicons.solid.FastForward
import compose.icons.fontawesomeicons.solid.Faucet
import compose.icons.fontawesomeicons.solid.Fax
import compose.icons.fontawesomeicons.solid.Feather
import compose.icons.fontawesomeicons.solid.FeatherAlt
import compose.icons.fontawesomeicons.solid.Female
import compose.icons.fontawesomeicons.solid.FighterJet
import compose.icons.fontawesomeicons.solid.File
import compose.icons.fontawesomeicons.solid.FileAlt
import compose.icons.fontawesomeicons.solid.FileArchive
import compose.icons.fontawesomeicons.solid.FileAudio
import compose.icons.fontawesomeicons.solid.FileCode
import compose.icons.fontawesomeicons.solid.FileContract
import compose.icons.fontawesomeicons.solid.FileCsv
import compose.icons.fontawesomeicons.solid.FileDownload
import compose.icons.fontawesomeicons.solid.FileExcel
import compose.icons.fontawesomeicons.solid.FileExport
import compose.icons.fontawesomeicons.solid.FileImage
import compose.icons.fontawesomeicons.solid.FileImport
import compose.icons.fontawesomeicons.solid.FileInvoice
import compose.icons.fontawesomeicons.solid.FileInvoiceDollar
import compose.icons.fontawesomeicons.solid.FileMedical
import compose.icons.fontawesomeicons.solid.FileMedicalAlt
import compose.icons.fontawesomeicons.solid.FilePdf
import compose.icons.fontawesomeicons.solid.FilePowerpoint
import compose.icons.fontawesomeicons.solid.FilePrescription
import compose.icons.fontawesomeicons.solid.FileSignature
import compose.icons.fontawesomeicons.solid.FileUpload
import compose.icons.fontawesomeicons.solid.FileVideo
import compose.icons.fontawesomeicons.solid.FileWord
import compose.icons.fontawesomeicons.solid.Fill
import compose.icons.fontawesomeicons.solid.FillDrip
import compose.icons.fontawesomeicons.solid.Film
import compose.icons.fontawesomeicons.solid.Filter
import compose.icons.fontawesomeicons.solid.Fingerprint
import compose.icons.fontawesomeicons.solid.Fire
import compose.icons.fontawesomeicons.solid.FireAlt
import compose.icons.fontawesomeicons.solid.FireExtinguisher
import compose.icons.fontawesomeicons.solid.FirstAid
import compose.icons.fontawesomeicons.solid.Fish
import compose.icons.fontawesomeicons.solid.FistRaised
import compose.icons.fontawesomeicons.solid.Flag
import compose.icons.fontawesomeicons.solid.FlagCheckered
import compose.icons.fontawesomeicons.solid.FlagUsa
import compose.icons.fontawesomeicons.solid.Flask
import compose.icons.fontawesomeicons.solid.Flushed
import compose.icons.fontawesomeicons.solid.Folder
import compose.icons.fontawesomeicons.solid.FolderMinus
import compose.icons.fontawesomeicons.solid.FolderOpen
import compose.icons.fontawesomeicons.solid.FolderPlus
import compose.icons.fontawesomeicons.solid.Font
import compose.icons.fontawesomeicons.solid.FontAwesomeLogoFull
import compose.icons.fontawesomeicons.solid.FootballBall
import compose.icons.fontawesomeicons.solid.Forward
import compose.icons.fontawesomeicons.solid.Frog
import compose.icons.fontawesomeicons.solid.Frown
import compose.icons.fontawesomeicons.solid.FrownOpen
import compose.icons.fontawesomeicons.solid.FunnelDollar
import compose.icons.fontawesomeicons.solid.Futbol
import compose.icons.fontawesomeicons.solid.Gamepad
import compose.icons.fontawesomeicons.solid.GasPump
import compose.icons.fontawesomeicons.solid.Gavel
import compose.icons.fontawesomeicons.solid.Gem
import compose.icons.fontawesomeicons.solid.Genderless
import compose.icons.fontawesomeicons.solid.Ghost
import compose.icons.fontawesomeicons.solid.Gift
import compose.icons.fontawesomeicons.solid.Gifts
import compose.icons.fontawesomeicons.solid.GlassCheers
import compose.icons.fontawesomeicons.solid.GlassMartini
import compose.icons.fontawesomeicons.solid.GlassMartiniAlt
import compose.icons.fontawesomeicons.solid.GlassWhiskey
import compose.icons.fontawesomeicons.solid.Glasses
import compose.icons.fontawesomeicons.solid.Globe
import compose.icons.fontawesomeicons.solid.GlobeAfrica
import compose.icons.fontawesomeicons.solid.GlobeAmericas
import compose.icons.fontawesomeicons.solid.GlobeAsia
import compose.icons.fontawesomeicons.solid.GlobeEurope
import compose.icons.fontawesomeicons.solid.GolfBall
import compose.icons.fontawesomeicons.solid.Gopuram
import compose.icons.fontawesomeicons.solid.GraduationCap
import compose.icons.fontawesomeicons.solid.GreaterThan
import compose.icons.fontawesomeicons.solid.GreaterThanEqual
import compose.icons.fontawesomeicons.solid.Grimace
import compose.icons.fontawesomeicons.solid.Grin
import compose.icons.fontawesomeicons.solid.GrinAlt
import compose.icons.fontawesomeicons.solid.GrinBeam
import compose.icons.fontawesomeicons.solid.GrinBeamSweat
import compose.icons.fontawesomeicons.solid.GrinHearts
import compose.icons.fontawesomeicons.solid.GrinSquint
import compose.icons.fontawesomeicons.solid.GrinSquintTears
import compose.icons.fontawesomeicons.solid.GrinStars
import compose.icons.fontawesomeicons.solid.GrinTears
import compose.icons.fontawesomeicons.solid.GrinTongue
import compose.icons.fontawesomeicons.solid.GrinTongueSquint
import compose.icons.fontawesomeicons.solid.GrinTongueWink
import compose.icons.fontawesomeicons.solid.GrinWink
import compose.icons.fontawesomeicons.solid.GripHorizontal
import compose.icons.fontawesomeicons.solid.GripLines
import compose.icons.fontawesomeicons.solid.GripLinesVertical
import compose.icons.fontawesomeicons.solid.GripVertical
import compose.icons.fontawesomeicons.solid.Guitar
import compose.icons.fontawesomeicons.solid.HSquare
import compose.icons.fontawesomeicons.solid.Hamburger
import compose.icons.fontawesomeicons.solid.Hammer
import compose.icons.fontawesomeicons.solid.Hamsa
import compose.icons.fontawesomeicons.solid.HandHolding
import compose.icons.fontawesomeicons.solid.HandHoldingHeart
import compose.icons.fontawesomeicons.solid.HandHoldingMedical
import compose.icons.fontawesomeicons.solid.HandHoldingUsd
import compose.icons.fontawesomeicons.solid.HandHoldingWater
import compose.icons.fontawesomeicons.solid.HandLizard
import compose.icons.fontawesomeicons.solid.HandMiddleFinger
import compose.icons.fontawesomeicons.solid.HandPaper
import compose.icons.fontawesomeicons.solid.HandPeace
import compose.icons.fontawesomeicons.solid.HandPointDown
import compose.icons.fontawesomeicons.solid.HandPointLeft
import compose.icons.fontawesomeicons.solid.HandPointRight
import compose.icons.fontawesomeicons.solid.HandPointUp
import compose.icons.fontawesomeicons.solid.HandPointer
import compose.icons.fontawesomeicons.solid.HandRock
import compose.icons.fontawesomeicons.solid.HandScissors
import compose.icons.fontawesomeicons.solid.HandSparkles
import compose.icons.fontawesomeicons.solid.HandSpock
import compose.icons.fontawesomeicons.solid.Hands
import compose.icons.fontawesomeicons.solid.HandsHelping
import compose.icons.fontawesomeicons.solid.HandsWash
import compose.icons.fontawesomeicons.solid.Handshake
import compose.icons.fontawesomeicons.solid.HandshakeAltSlash
import compose.icons.fontawesomeicons.solid.HandshakeSlash
import compose.icons.fontawesomeicons.solid.Hanukiah
import compose.icons.fontawesomeicons.solid.HardHat
import compose.icons.fontawesomeicons.solid.Hashtag
import compose.icons.fontawesomeicons.solid.HatCowboy
import compose.icons.fontawesomeicons.solid.HatCowboySide
import compose.icons.fontawesomeicons.solid.HatWizard
import compose.icons.fontawesomeicons.solid.Hdd
import compose.icons.fontawesomeicons.solid.HeadSideCough
import compose.icons.fontawesomeicons.solid.HeadSideCoughSlash
import compose.icons.fontawesomeicons.solid.HeadSideMask
import compose.icons.fontawesomeicons.solid.HeadSideVirus
import compose.icons.fontawesomeicons.solid.Heading
import compose.icons.fontawesomeicons.solid.Headphones
import compose.icons.fontawesomeicons.solid.HeadphonesAlt
import compose.icons.fontawesomeicons.solid.Headset
import compose.icons.fontawesomeicons.solid.Heart
import compose.icons.fontawesomeicons.solid.HeartBroken
import compose.icons.fontawesomeicons.solid.Heartbeat
import compose.icons.fontawesomeicons.solid.Helicopter
import compose.icons.fontawesomeicons.solid.Highlighter
import compose.icons.fontawesomeicons.solid.Hiking
import compose.icons.fontawesomeicons.solid.Hippo
import compose.icons.fontawesomeicons.solid.History
import compose.icons.fontawesomeicons.solid.HockeyPuck
import compose.icons.fontawesomeicons.solid.HollyBerry
import compose.icons.fontawesomeicons.solid.Home
import compose.icons.fontawesomeicons.solid.Horse
import compose.icons.fontawesomeicons.solid.HorseHead
import compose.icons.fontawesomeicons.solid.Hospital
import compose.icons.fontawesomeicons.solid.HospitalAlt
import compose.icons.fontawesomeicons.solid.HospitalSymbol
import compose.icons.fontawesomeicons.solid.HospitalUser
import compose.icons.fontawesomeicons.solid.HotTub
import compose.icons.fontawesomeicons.solid.Hotdog
import compose.icons.fontawesomeicons.solid.Hotel
import compose.icons.fontawesomeicons.solid.Hourglass
import compose.icons.fontawesomeicons.solid.HourglassEnd
import compose.icons.fontawesomeicons.solid.HourglassHalf
import compose.icons.fontawesomeicons.solid.HourglassStart
import compose.icons.fontawesomeicons.solid.HouseDamage
import compose.icons.fontawesomeicons.solid.HouseUser
import compose.icons.fontawesomeicons.solid.Hryvnia
import compose.icons.fontawesomeicons.solid.ICursor
import compose.icons.fontawesomeicons.solid.IceCream
import compose.icons.fontawesomeicons.solid.Icicles
import compose.icons.fontawesomeicons.solid.Icons
import compose.icons.fontawesomeicons.solid.IdBadge
import compose.icons.fontawesomeicons.solid.IdCard
import compose.icons.fontawesomeicons.solid.IdCardAlt
import compose.icons.fontawesomeicons.solid.Igloo
import compose.icons.fontawesomeicons.solid.Image
import compose.icons.fontawesomeicons.solid.Images
import compose.icons.fontawesomeicons.solid.Inbox
import compose.icons.fontawesomeicons.solid.Indent
import compose.icons.fontawesomeicons.solid.Industry
import compose.icons.fontawesomeicons.solid.Infinity
import compose.icons.fontawesomeicons.solid.Info
import compose.icons.fontawesomeicons.solid.InfoCircle
import compose.icons.fontawesomeicons.solid.Italic
import compose.icons.fontawesomeicons.solid.Jedi
import compose.icons.fontawesomeicons.solid.Joint
import compose.icons.fontawesomeicons.solid.JournalWhills
import compose.icons.fontawesomeicons.solid.Kaaba
import compose.icons.fontawesomeicons.solid.Key
import compose.icons.fontawesomeicons.solid.Keyboard
import compose.icons.fontawesomeicons.solid.Khanda
import compose.icons.fontawesomeicons.solid.Kiss
import compose.icons.fontawesomeicons.solid.KissBeam
import compose.icons.fontawesomeicons.solid.KissWinkHeart
import compose.icons.fontawesomeicons.solid.KiwiBird
import compose.icons.fontawesomeicons.solid.Landmark
import compose.icons.fontawesomeicons.solid.Language
import compose.icons.fontawesomeicons.solid.Laptop
import compose.icons.fontawesomeicons.solid.LaptopCode
import compose.icons.fontawesomeicons.solid.LaptopHouse
import compose.icons.fontawesomeicons.solid.LaptopMedical
import compose.icons.fontawesomeicons.solid.Laugh
import compose.icons.fontawesomeicons.solid.LaughBeam
import compose.icons.fontawesomeicons.solid.LaughSquint
import compose.icons.fontawesomeicons.solid.LaughWink
import compose.icons.fontawesomeicons.solid.LayerGroup
import compose.icons.fontawesomeicons.solid.Leaf
import compose.icons.fontawesomeicons.solid.Lemon
import compose.icons.fontawesomeicons.solid.LessThan
import compose.icons.fontawesomeicons.solid.LessThanEqual
import compose.icons.fontawesomeicons.solid.LevelDownAlt
import compose.icons.fontawesomeicons.solid.LevelUpAlt
import compose.icons.fontawesomeicons.solid.LifeRing
import compose.icons.fontawesomeicons.solid.Lightbulb
import compose.icons.fontawesomeicons.solid.Link
import compose.icons.fontawesomeicons.solid.LiraSign
import compose.icons.fontawesomeicons.solid.List
import compose.icons.fontawesomeicons.solid.ListAlt
import compose.icons.fontawesomeicons.solid.ListOl
import compose.icons.fontawesomeicons.solid.ListUl
import compose.icons.fontawesomeicons.solid.LocationArrow
import compose.icons.fontawesomeicons.solid.Lock
import compose.icons.fontawesomeicons.solid.LockOpen
import compose.icons.fontawesomeicons.solid.LongArrowAltDown
import compose.icons.fontawesomeicons.solid.LongArrowAltLeft
import compose.icons.fontawesomeicons.solid.LongArrowAltRight
import compose.icons.fontawesomeicons.solid.LongArrowAltUp
import compose.icons.fontawesomeicons.solid.LowVision
import compose.icons.fontawesomeicons.solid.LuggageCart
import compose.icons.fontawesomeicons.solid.Lungs
import compose.icons.fontawesomeicons.solid.LungsVirus
import compose.icons.fontawesomeicons.solid.Magic
import compose.icons.fontawesomeicons.solid.Magnet
import compose.icons.fontawesomeicons.solid.MailBulk
import compose.icons.fontawesomeicons.solid.Male
import compose.icons.fontawesomeicons.solid.Map
import compose.icons.fontawesomeicons.solid.MapMarked
import compose.icons.fontawesomeicons.solid.MapMarkedAlt
import compose.icons.fontawesomeicons.solid.MapMarker
import compose.icons.fontawesomeicons.solid.MapMarkerAlt
import compose.icons.fontawesomeicons.solid.MapPin
import compose.icons.fontawesomeicons.solid.MapSigns
import compose.icons.fontawesomeicons.solid.Marker
import compose.icons.fontawesomeicons.solid.Mars
import compose.icons.fontawesomeicons.solid.MarsDouble
import compose.icons.fontawesomeicons.solid.MarsStroke
import compose.icons.fontawesomeicons.solid.MarsStrokeH
import compose.icons.fontawesomeicons.solid.MarsStrokeV
import compose.icons.fontawesomeicons.solid.Mask
import compose.icons.fontawesomeicons.solid.Medal
import compose.icons.fontawesomeicons.solid.Medkit
import compose.icons.fontawesomeicons.solid.Meh
import compose.icons.fontawesomeicons.solid.MehBlank
import compose.icons.fontawesomeicons.solid.MehRollingEyes
import compose.icons.fontawesomeicons.solid.Memory
import compose.icons.fontawesomeicons.solid.Menorah
import compose.icons.fontawesomeicons.solid.Mercury
import compose.icons.fontawesomeicons.solid.Meteor
import compose.icons.fontawesomeicons.solid.Microchip
import compose.icons.fontawesomeicons.solid.Microphone
import compose.icons.fontawesomeicons.solid.MicrophoneAlt
import compose.icons.fontawesomeicons.solid.MicrophoneAltSlash
import compose.icons.fontawesomeicons.solid.MicrophoneSlash
import compose.icons.fontawesomeicons.solid.Microscope
import compose.icons.fontawesomeicons.solid.Minus
import compose.icons.fontawesomeicons.solid.MinusCircle
import compose.icons.fontawesomeicons.solid.MinusSquare
import compose.icons.fontawesomeicons.solid.Mitten
import compose.icons.fontawesomeicons.solid.Mobile
import compose.icons.fontawesomeicons.solid.MobileAlt
import compose.icons.fontawesomeicons.solid.MoneyBill
import compose.icons.fontawesomeicons.solid.MoneyBillAlt
import compose.icons.fontawesomeicons.solid.MoneyBillWave
import compose.icons.fontawesomeicons.solid.MoneyBillWaveAlt
import compose.icons.fontawesomeicons.solid.MoneyCheck
import compose.icons.fontawesomeicons.solid.MoneyCheckAlt
import compose.icons.fontawesomeicons.solid.Monument
import compose.icons.fontawesomeicons.solid.Moon
import compose.icons.fontawesomeicons.solid.MortarPestle
import compose.icons.fontawesomeicons.solid.Mosque
import compose.icons.fontawesomeicons.solid.Motorcycle
import compose.icons.fontawesomeicons.solid.Mountain
import compose.icons.fontawesomeicons.solid.Mouse
import compose.icons.fontawesomeicons.solid.MousePointer
import compose.icons.fontawesomeicons.solid.MugHot
import compose.icons.fontawesomeicons.solid.Music
import compose.icons.fontawesomeicons.solid.NetworkWired
import compose.icons.fontawesomeicons.solid.Neuter
import compose.icons.fontawesomeicons.solid.Newspaper
import compose.icons.fontawesomeicons.solid.NotEqual
import compose.icons.fontawesomeicons.solid.NotesMedical
import compose.icons.fontawesomeicons.solid.ObjectGroup
import compose.icons.fontawesomeicons.solid.ObjectUngroup
import compose.icons.fontawesomeicons.solid.OilCan
import compose.icons.fontawesomeicons.solid.Om
import compose.icons.fontawesomeicons.solid.Otter
import compose.icons.fontawesomeicons.solid.Outdent
import compose.icons.fontawesomeicons.solid.Pager
import compose.icons.fontawesomeicons.solid.PaintBrush
import compose.icons.fontawesomeicons.solid.PaintRoller
import compose.icons.fontawesomeicons.solid.Palette
import compose.icons.fontawesomeicons.solid.Pallet
import compose.icons.fontawesomeicons.solid.PaperPlane
import compose.icons.fontawesomeicons.solid.Paperclip
import compose.icons.fontawesomeicons.solid.ParachuteBox
import compose.icons.fontawesomeicons.solid.Paragraph
import compose.icons.fontawesomeicons.solid.Parking
import compose.icons.fontawesomeicons.solid.Passport
import compose.icons.fontawesomeicons.solid.Pastafarianism
import compose.icons.fontawesomeicons.solid.Paste
import compose.icons.fontawesomeicons.solid.Pause
import compose.icons.fontawesomeicons.solid.PauseCircle
import compose.icons.fontawesomeicons.solid.Paw
import compose.icons.fontawesomeicons.solid.Peace
import compose.icons.fontawesomeicons.solid.Pen
import compose.icons.fontawesomeicons.solid.PenAlt
import compose.icons.fontawesomeicons.solid.PenFancy
import compose.icons.fontawesomeicons.solid.PenNib
import compose.icons.fontawesomeicons.solid.PenSquare
import compose.icons.fontawesomeicons.solid.PencilAlt
import compose.icons.fontawesomeicons.solid.PencilRuler
import compose.icons.fontawesomeicons.solid.PeopleArrows
import compose.icons.fontawesomeicons.solid.PeopleCarry
import compose.icons.fontawesomeicons.solid.PepperHot
import compose.icons.fontawesomeicons.solid.Percent
import compose.icons.fontawesomeicons.solid.Percentage
import compose.icons.fontawesomeicons.solid.PersonBooth
import compose.icons.fontawesomeicons.solid.Phone
import compose.icons.fontawesomeicons.solid.PhoneAlt
import compose.icons.fontawesomeicons.solid.PhoneSlash
import compose.icons.fontawesomeicons.solid.PhoneSquare
import compose.icons.fontawesomeicons.solid.PhoneSquareAlt
import compose.icons.fontawesomeicons.solid.PhoneVolume
import compose.icons.fontawesomeicons.solid.PhotoVideo
import compose.icons.fontawesomeicons.solid.PiggyBank
import compose.icons.fontawesomeicons.solid.Pills
import compose.icons.fontawesomeicons.solid.PizzaSlice
import compose.icons.fontawesomeicons.solid.PlaceOfWorship
import compose.icons.fontawesomeicons.solid.Plane
import compose.icons.fontawesomeicons.solid.PlaneArrival
import compose.icons.fontawesomeicons.solid.PlaneDeparture
import compose.icons.fontawesomeicons.solid.PlaneSlash
import compose.icons.fontawesomeicons.solid.Play
import compose.icons.fontawesomeicons.solid.PlayCircle
import compose.icons.fontawesomeicons.solid.Plug
import compose.icons.fontawesomeicons.solid.Plus
import compose.icons.fontawesomeicons.solid.PlusCircle
import compose.icons.fontawesomeicons.solid.PlusSquare
import compose.icons.fontawesomeicons.solid.Podcast
import compose.icons.fontawesomeicons.solid.Poll
import compose.icons.fontawesomeicons.solid.PollH
import compose.icons.fontawesomeicons.solid.Poo
import compose.icons.fontawesomeicons.solid.PooStorm
import compose.icons.fontawesomeicons.solid.Poop
import compose.icons.fontawesomeicons.solid.Portrait
import compose.icons.fontawesomeicons.solid.PoundSign
import compose.icons.fontawesomeicons.solid.PowerOff
import compose.icons.fontawesomeicons.solid.Pray
import compose.icons.fontawesomeicons.solid.PrayingHands
import compose.icons.fontawesomeicons.solid.Prescription
import compose.icons.fontawesomeicons.solid.PrescriptionBottle
import compose.icons.fontawesomeicons.solid.PrescriptionBottleAlt
import compose.icons.fontawesomeicons.solid.Print
import compose.icons.fontawesomeicons.solid.Procedures
import compose.icons.fontawesomeicons.solid.ProjectDiagram
import compose.icons.fontawesomeicons.solid.PumpMedical
import compose.icons.fontawesomeicons.solid.PumpSoap
import compose.icons.fontawesomeicons.solid.PuzzlePiece
import compose.icons.fontawesomeicons.solid.Qrcode
import compose.icons.fontawesomeicons.solid.Question
import compose.icons.fontawesomeicons.solid.QuestionCircle
import compose.icons.fontawesomeicons.solid.Quidditch
import compose.icons.fontawesomeicons.solid.QuoteLeft
import compose.icons.fontawesomeicons.solid.QuoteRight
import compose.icons.fontawesomeicons.solid.Quran
import compose.icons.fontawesomeicons.solid.Radiation
import compose.icons.fontawesomeicons.solid.RadiationAlt
import compose.icons.fontawesomeicons.solid.Rainbow
import compose.icons.fontawesomeicons.solid.Random
import compose.icons.fontawesomeicons.solid.Receipt
import compose.icons.fontawesomeicons.solid.RecordVinyl
import compose.icons.fontawesomeicons.solid.Recycle
import compose.icons.fontawesomeicons.solid.Redo
import compose.icons.fontawesomeicons.solid.RedoAlt
import compose.icons.fontawesomeicons.solid.Registered
import compose.icons.fontawesomeicons.solid.RemoveFormat
import compose.icons.fontawesomeicons.solid.Reply
import compose.icons.fontawesomeicons.solid.ReplyAll
import compose.icons.fontawesomeicons.solid.Republican
import compose.icons.fontawesomeicons.solid.Restroom
import compose.icons.fontawesomeicons.solid.Retweet
import compose.icons.fontawesomeicons.solid.Ribbon
import compose.icons.fontawesomeicons.solid.Ring
import compose.icons.fontawesomeicons.solid.Road
import compose.icons.fontawesomeicons.solid.Robot
import compose.icons.fontawesomeicons.solid.Rocket
import compose.icons.fontawesomeicons.solid.Route
import compose.icons.fontawesomeicons.solid.Rss
import compose.icons.fontawesomeicons.solid.RssSquare
import compose.icons.fontawesomeicons.solid.RubleSign
import compose.icons.fontawesomeicons.solid.Ruler
import compose.icons.fontawesomeicons.solid.RulerCombined
import compose.icons.fontawesomeicons.solid.RulerHorizontal
import compose.icons.fontawesomeicons.solid.RulerVertical
import compose.icons.fontawesomeicons.solid.Running
import compose.icons.fontawesomeicons.solid.RupeeSign
import compose.icons.fontawesomeicons.solid.SadCry
import compose.icons.fontawesomeicons.solid.SadTear
import compose.icons.fontawesomeicons.solid.Satellite
import compose.icons.fontawesomeicons.solid.SatelliteDish
import compose.icons.fontawesomeicons.solid.Save
import compose.icons.fontawesomeicons.solid.School
import compose.icons.fontawesomeicons.solid.Screwdriver
import compose.icons.fontawesomeicons.solid.Scroll
import compose.icons.fontawesomeicons.solid.SdCard
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.SearchDollar
import compose.icons.fontawesomeicons.solid.SearchLocation
import compose.icons.fontawesomeicons.solid.SearchMinus
import compose.icons.fontawesomeicons.solid.SearchPlus
import compose.icons.fontawesomeicons.solid.Seedling
import compose.icons.fontawesomeicons.solid.Server
import compose.icons.fontawesomeicons.solid.Shapes
import compose.icons.fontawesomeicons.solid.Share
import compose.icons.fontawesomeicons.solid.ShareAlt
import compose.icons.fontawesomeicons.solid.ShareAltSquare
import compose.icons.fontawesomeicons.solid.ShareSquare
import compose.icons.fontawesomeicons.solid.ShekelSign
import compose.icons.fontawesomeicons.solid.ShieldAlt
import compose.icons.fontawesomeicons.solid.ShieldVirus
import compose.icons.fontawesomeicons.solid.Ship
import compose.icons.fontawesomeicons.solid.ShippingFast
import compose.icons.fontawesomeicons.solid.ShoePrints
import compose.icons.fontawesomeicons.solid.ShoppingBag
import compose.icons.fontawesomeicons.solid.ShoppingBasket
import compose.icons.fontawesomeicons.solid.ShoppingCart
import compose.icons.fontawesomeicons.solid.Shower
import compose.icons.fontawesomeicons.solid.ShuttleVan
import compose.icons.fontawesomeicons.solid.Sign
import compose.icons.fontawesomeicons.solid.SignInAlt
import compose.icons.fontawesomeicons.solid.SignLanguage
import compose.icons.fontawesomeicons.solid.SignOutAlt
import compose.icons.fontawesomeicons.solid.Signal
import compose.icons.fontawesomeicons.solid.Signature
import compose.icons.fontawesomeicons.solid.SimCard
import compose.icons.fontawesomeicons.solid.Sink
import compose.icons.fontawesomeicons.solid.Sitemap
import compose.icons.fontawesomeicons.solid.Skating
import compose.icons.fontawesomeicons.solid.Skiing
import compose.icons.fontawesomeicons.solid.SkiingNordic
import compose.icons.fontawesomeicons.solid.Skull
import compose.icons.fontawesomeicons.solid.SkullCrossbones
import compose.icons.fontawesomeicons.solid.Slash
import compose.icons.fontawesomeicons.solid.Sleigh
import compose.icons.fontawesomeicons.solid.SlidersH
import compose.icons.fontawesomeicons.solid.Smile
import compose.icons.fontawesomeicons.solid.SmileBeam
import compose.icons.fontawesomeicons.solid.SmileWink
import compose.icons.fontawesomeicons.solid.Smog
import compose.icons.fontawesomeicons.solid.Smoking
import compose.icons.fontawesomeicons.solid.SmokingBan
import compose.icons.fontawesomeicons.solid.Sms
import compose.icons.fontawesomeicons.solid.Snowboarding
import compose.icons.fontawesomeicons.solid.Snowflake
import compose.icons.fontawesomeicons.solid.Snowman
import compose.icons.fontawesomeicons.solid.Snowplow
import compose.icons.fontawesomeicons.solid.Soap
import compose.icons.fontawesomeicons.solid.Socks
import compose.icons.fontawesomeicons.solid.SolarPanel
import compose.icons.fontawesomeicons.solid.Sort
import compose.icons.fontawesomeicons.solid.SortAlphaDown
import compose.icons.fontawesomeicons.solid.SortAlphaDownAlt
import compose.icons.fontawesomeicons.solid.SortAlphaUp
import compose.icons.fontawesomeicons.solid.SortAlphaUpAlt
import compose.icons.fontawesomeicons.solid.SortAmountDown
import compose.icons.fontawesomeicons.solid.SortAmountDownAlt
import compose.icons.fontawesomeicons.solid.SortAmountUp
import compose.icons.fontawesomeicons.solid.SortAmountUpAlt
import compose.icons.fontawesomeicons.solid.SortDown
import compose.icons.fontawesomeicons.solid.SortNumericDown
import compose.icons.fontawesomeicons.solid.SortNumericDownAlt
import compose.icons.fontawesomeicons.solid.SortNumericUp
import compose.icons.fontawesomeicons.solid.SortNumericUpAlt
import compose.icons.fontawesomeicons.solid.SortUp
import compose.icons.fontawesomeicons.solid.Spa
import compose.icons.fontawesomeicons.solid.SpaceShuttle
import compose.icons.fontawesomeicons.solid.SpellCheck
import compose.icons.fontawesomeicons.solid.Spider
import compose.icons.fontawesomeicons.solid.Spinner
import compose.icons.fontawesomeicons.solid.Splotch
import compose.icons.fontawesomeicons.solid.SprayCan
import compose.icons.fontawesomeicons.solid.Square
import compose.icons.fontawesomeicons.solid.SquareFull
import compose.icons.fontawesomeicons.solid.SquareRootAlt
import compose.icons.fontawesomeicons.solid.Stamp
import compose.icons.fontawesomeicons.solid.Star
import compose.icons.fontawesomeicons.solid.StarAndCrescent
import compose.icons.fontawesomeicons.solid.StarHalf
import compose.icons.fontawesomeicons.solid.StarHalfAlt
import compose.icons.fontawesomeicons.solid.StarOfDavid
import compose.icons.fontawesomeicons.solid.StarOfLife
import compose.icons.fontawesomeicons.solid.StepBackward
import compose.icons.fontawesomeicons.solid.StepForward
import compose.icons.fontawesomeicons.solid.Stethoscope
import compose.icons.fontawesomeicons.solid.StickyNote
import compose.icons.fontawesomeicons.solid.Stop
import compose.icons.fontawesomeicons.solid.StopCircle
import compose.icons.fontawesomeicons.solid.Stopwatch
import compose.icons.fontawesomeicons.solid.Stopwatch20
import compose.icons.fontawesomeicons.solid.Store
import compose.icons.fontawesomeicons.solid.StoreAlt
import compose.icons.fontawesomeicons.solid.StoreAltSlash
import compose.icons.fontawesomeicons.solid.StoreSlash
import compose.icons.fontawesomeicons.solid.Stream
import compose.icons.fontawesomeicons.solid.StreetView
import compose.icons.fontawesomeicons.solid.Strikethrough
import compose.icons.fontawesomeicons.solid.Stroopwafel
import compose.icons.fontawesomeicons.solid.Subscript
import compose.icons.fontawesomeicons.solid.Subway
import compose.icons.fontawesomeicons.solid.Suitcase
import compose.icons.fontawesomeicons.solid.SuitcaseRolling
import compose.icons.fontawesomeicons.solid.Sun
import compose.icons.fontawesomeicons.solid.Superscript
import compose.icons.fontawesomeicons.solid.Surprise
import compose.icons.fontawesomeicons.solid.Swatchbook
import compose.icons.fontawesomeicons.solid.Swimmer
import compose.icons.fontawesomeicons.solid.SwimmingPool
import compose.icons.fontawesomeicons.solid.Synagogue
import compose.icons.fontawesomeicons.solid.Sync
import compose.icons.fontawesomeicons.solid.SyncAlt
import compose.icons.fontawesomeicons.solid.Syringe
import compose.icons.fontawesomeicons.solid.Table
import compose.icons.fontawesomeicons.solid.TableTennis
import compose.icons.fontawesomeicons.solid.Tablet
import compose.icons.fontawesomeicons.solid.TabletAlt
import compose.icons.fontawesomeicons.solid.Tablets
import compose.icons.fontawesomeicons.solid.TachometerAlt
import compose.icons.fontawesomeicons.solid.Tag
import compose.icons.fontawesomeicons.solid.Tags
import compose.icons.fontawesomeicons.solid.Tape
import compose.icons.fontawesomeicons.solid.Tasks
import compose.icons.fontawesomeicons.solid.Taxi
import compose.icons.fontawesomeicons.solid.Teeth
import compose.icons.fontawesomeicons.solid.TeethOpen
import compose.icons.fontawesomeicons.solid.TemperatureHigh
import compose.icons.fontawesomeicons.solid.TemperatureLow
import compose.icons.fontawesomeicons.solid.Tenge
import compose.icons.fontawesomeicons.solid.Terminal
import compose.icons.fontawesomeicons.solid.TextHeight
import compose.icons.fontawesomeicons.solid.TextWidth
import compose.icons.fontawesomeicons.solid.Th
import compose.icons.fontawesomeicons.solid.ThLarge
import compose.icons.fontawesomeicons.solid.ThList
import compose.icons.fontawesomeicons.solid.TheaterMasks
import compose.icons.fontawesomeicons.solid.Thermometer
import compose.icons.fontawesomeicons.solid.ThermometerEmpty
import compose.icons.fontawesomeicons.solid.ThermometerFull
import compose.icons.fontawesomeicons.solid.ThermometerHalf
import compose.icons.fontawesomeicons.solid.ThermometerQuarter
import compose.icons.fontawesomeicons.solid.ThermometerThreeQuarters
import compose.icons.fontawesomeicons.solid.ThumbsDown
import compose.icons.fontawesomeicons.solid.ThumbsUp
import compose.icons.fontawesomeicons.solid.Thumbtack
import compose.icons.fontawesomeicons.solid.TicketAlt
import compose.icons.fontawesomeicons.solid.Times
import compose.icons.fontawesomeicons.solid.TimesCircle
import compose.icons.fontawesomeicons.solid.Tint
import compose.icons.fontawesomeicons.solid.TintSlash
import compose.icons.fontawesomeicons.solid.Tired
import compose.icons.fontawesomeicons.solid.ToggleOff
import compose.icons.fontawesomeicons.solid.ToggleOn
import compose.icons.fontawesomeicons.solid.Toilet
import compose.icons.fontawesomeicons.solid.ToiletPaper
import compose.icons.fontawesomeicons.solid.ToiletPaperSlash
import compose.icons.fontawesomeicons.solid.Toolbox
import compose.icons.fontawesomeicons.solid.Tools
import compose.icons.fontawesomeicons.solid.Tooth
import compose.icons.fontawesomeicons.solid.Torah
import compose.icons.fontawesomeicons.solid.ToriiGate
import compose.icons.fontawesomeicons.solid.Tractor
import compose.icons.fontawesomeicons.solid.Trademark
import compose.icons.fontawesomeicons.solid.TrafficLight
import compose.icons.fontawesomeicons.solid.Trailer
import compose.icons.fontawesomeicons.solid.Train
import compose.icons.fontawesomeicons.solid.Tram
import compose.icons.fontawesomeicons.solid.Transgender
import compose.icons.fontawesomeicons.solid.TransgenderAlt
import compose.icons.fontawesomeicons.solid.Trash
import compose.icons.fontawesomeicons.solid.TrashAlt
import compose.icons.fontawesomeicons.solid.TrashRestore
import compose.icons.fontawesomeicons.solid.TrashRestoreAlt
import compose.icons.fontawesomeicons.solid.Tree
import compose.icons.fontawesomeicons.solid.Trophy
import compose.icons.fontawesomeicons.solid.Truck
import compose.icons.fontawesomeicons.solid.TruckLoading
import compose.icons.fontawesomeicons.solid.TruckMonster
import compose.icons.fontawesomeicons.solid.TruckMoving
import compose.icons.fontawesomeicons.solid.TruckPickup
import compose.icons.fontawesomeicons.solid.Tshirt
import compose.icons.fontawesomeicons.solid.Tty
import compose.icons.fontawesomeicons.solid.Tv
import compose.icons.fontawesomeicons.solid.Umbrella
import compose.icons.fontawesomeicons.solid.UmbrellaBeach
import compose.icons.fontawesomeicons.solid.Underline
import compose.icons.fontawesomeicons.solid.Undo
import compose.icons.fontawesomeicons.solid.UndoAlt
import compose.icons.fontawesomeicons.solid.UniversalAccess
import compose.icons.fontawesomeicons.solid.University
import compose.icons.fontawesomeicons.solid.Unlink
import compose.icons.fontawesomeicons.solid.Unlock
import compose.icons.fontawesomeicons.solid.UnlockAlt
import compose.icons.fontawesomeicons.solid.Upload
import compose.icons.fontawesomeicons.solid.User
import compose.icons.fontawesomeicons.solid.UserAlt
import compose.icons.fontawesomeicons.solid.UserAltSlash
import compose.icons.fontawesomeicons.solid.UserAstronaut
import compose.icons.fontawesomeicons.solid.UserCheck
import compose.icons.fontawesomeicons.solid.UserCircle
import compose.icons.fontawesomeicons.solid.UserClock
import compose.icons.fontawesomeicons.solid.UserCog
import compose.icons.fontawesomeicons.solid.UserEdit
import compose.icons.fontawesomeicons.solid.UserFriends
import compose.icons.fontawesomeicons.solid.UserGraduate
import compose.icons.fontawesomeicons.solid.UserInjured
import compose.icons.fontawesomeicons.solid.UserLock
import compose.icons.fontawesomeicons.solid.UserMd
import compose.icons.fontawesomeicons.solid.UserMinus
import compose.icons.fontawesomeicons.solid.UserNinja
import compose.icons.fontawesomeicons.solid.UserNurse
import compose.icons.fontawesomeicons.solid.UserPlus
import compose.icons.fontawesomeicons.solid.UserSecret
import compose.icons.fontawesomeicons.solid.UserShield
import compose.icons.fontawesomeicons.solid.UserSlash
import compose.icons.fontawesomeicons.solid.UserTag
import compose.icons.fontawesomeicons.solid.UserTie
import compose.icons.fontawesomeicons.solid.UserTimes
import compose.icons.fontawesomeicons.solid.Users
import compose.icons.fontawesomeicons.solid.UsersCog
import compose.icons.fontawesomeicons.solid.UsersSlash
import compose.icons.fontawesomeicons.solid.UtensilSpoon
import compose.icons.fontawesomeicons.solid.Utensils
import compose.icons.fontawesomeicons.solid.VectorSquare
import compose.icons.fontawesomeicons.solid.Venus
import compose.icons.fontawesomeicons.solid.VenusDouble
import compose.icons.fontawesomeicons.solid.VenusMars
import compose.icons.fontawesomeicons.solid.Vest
import compose.icons.fontawesomeicons.solid.VestPatches
import compose.icons.fontawesomeicons.solid.Vial
import compose.icons.fontawesomeicons.solid.Vials
import compose.icons.fontawesomeicons.solid.Video
import compose.icons.fontawesomeicons.solid.VideoSlash
import compose.icons.fontawesomeicons.solid.Vihara
import compose.icons.fontawesomeicons.solid.Virus
import compose.icons.fontawesomeicons.solid.VirusSlash
import compose.icons.fontawesomeicons.solid.Viruses
import compose.icons.fontawesomeicons.solid.Voicemail
import compose.icons.fontawesomeicons.solid.VolleyballBall
import compose.icons.fontawesomeicons.solid.VolumeDown
import compose.icons.fontawesomeicons.solid.VolumeMute
import compose.icons.fontawesomeicons.solid.VolumeOff
import compose.icons.fontawesomeicons.solid.VolumeUp
import compose.icons.fontawesomeicons.solid.VoteYea
import compose.icons.fontawesomeicons.solid.VrCardboard
import compose.icons.fontawesomeicons.solid.Walking
import compose.icons.fontawesomeicons.solid.Wallet
import compose.icons.fontawesomeicons.solid.Warehouse
import compose.icons.fontawesomeicons.solid.Water
import compose.icons.fontawesomeicons.solid.WaveSquare
import compose.icons.fontawesomeicons.solid.Weight
import compose.icons.fontawesomeicons.solid.WeightHanging
import compose.icons.fontawesomeicons.solid.Wheelchair
import compose.icons.fontawesomeicons.solid.Wifi
import compose.icons.fontawesomeicons.solid.Wind
import compose.icons.fontawesomeicons.solid.WindowClose
import compose.icons.fontawesomeicons.solid.WindowMaximize
import compose.icons.fontawesomeicons.solid.WindowMinimize
import compose.icons.fontawesomeicons.solid.WindowRestore
import compose.icons.fontawesomeicons.solid.WineBottle
import compose.icons.fontawesomeicons.solid.WineGlass
import compose.icons.fontawesomeicons.solid.WineGlassAlt
import compose.icons.fontawesomeicons.solid.WonSign
import compose.icons.fontawesomeicons.solid.Wrench
import compose.icons.fontawesomeicons.solid.XRay
import compose.icons.fontawesomeicons.solid.YenSign
import compose.icons.fontawesomeicons.solid.YinYang

object Icons {

    fun getIcons(name: String): ImageVector? = allIcons[name]

    val regularIcons: Map<String, ImageVector> by lazy {
        mapOf(
            "AddressBook" to FontAwesomeIcons.Regular.AddressBook,
            "AddressCard" to FontAwesomeIcons.Regular.AddressCard,
            "Angry" to FontAwesomeIcons.Regular.Angry,
            "ArrowAltCircleDown" to FontAwesomeIcons.Regular.ArrowAltCircleDown,
            "ArrowAltCircleLeft" to FontAwesomeIcons.Regular.ArrowAltCircleLeft,
            "ArrowAltCircleRight" to FontAwesomeIcons.Regular.ArrowAltCircleRight,
            "ArrowAltCircleUp" to FontAwesomeIcons.Regular.ArrowAltCircleUp,
            "Bell" to FontAwesomeIcons.Regular.Bell,
            "BellSlash" to FontAwesomeIcons.Regular.BellSlash,
            "Bookmark" to FontAwesomeIcons.Regular.Bookmark,
            "Building" to FontAwesomeIcons.Regular.Building,
            "Calendar" to FontAwesomeIcons.Regular.Calendar,
            "CalendarAlt" to FontAwesomeIcons.Regular.CalendarAlt,
            "CalendarCheck" to FontAwesomeIcons.Regular.CalendarCheck,
            "CalendarMinus" to FontAwesomeIcons.Regular.CalendarMinus,
            "CalendarPlus" to FontAwesomeIcons.Regular.CalendarPlus,
            "CalendarTimes" to FontAwesomeIcons.Regular.CalendarTimes,
            "CaretSquareDown" to FontAwesomeIcons.Regular.CaretSquareDown,
            "CaretSquareLeft" to FontAwesomeIcons.Regular.CaretSquareLeft,
            "CaretSquareRight" to FontAwesomeIcons.Regular.CaretSquareRight,
            "CaretSquareUp" to FontAwesomeIcons.Regular.CaretSquareUp,
            "ChartBar" to FontAwesomeIcons.Regular.ChartBar,
            "CheckCircle" to FontAwesomeIcons.Regular.CheckCircle,
            "CheckSquare" to FontAwesomeIcons.Regular.CheckSquare,
            "Circle" to FontAwesomeIcons.Regular.Circle,
            "Clipboard" to FontAwesomeIcons.Regular.Clipboard,
            "Clock" to FontAwesomeIcons.Regular.Clock,
            "Clone" to FontAwesomeIcons.Regular.Clone,
            "ClosedCaptioning" to FontAwesomeIcons.Regular.ClosedCaptioning,
            "Comment" to FontAwesomeIcons.Regular.Comment,
            "CommentAlt" to FontAwesomeIcons.Regular.CommentAlt,
            "CommentDots" to FontAwesomeIcons.Regular.CommentDots,
            "Comments" to FontAwesomeIcons.Regular.Comments,
            "Compass" to FontAwesomeIcons.Regular.Compass,
            "Copy" to FontAwesomeIcons.Regular.Copy,
            "Copyright" to FontAwesomeIcons.Regular.Copyright,
            "CreditCard" to FontAwesomeIcons.Regular.CreditCard,
            "Dizzy" to FontAwesomeIcons.Regular.Dizzy,
            "DotCircle" to FontAwesomeIcons.Regular.DotCircle,
            "Edit" to FontAwesomeIcons.Regular.Edit,
            "Envelope" to FontAwesomeIcons.Regular.Envelope,
            "EnvelopeOpen" to FontAwesomeIcons.Regular.EnvelopeOpen,
            "Eye" to FontAwesomeIcons.Regular.Eye,
            "EyeSlash" to FontAwesomeIcons.Regular.EyeSlash,
            "File" to FontAwesomeIcons.Regular.File,
            "FileAlt" to FontAwesomeIcons.Regular.FileAlt,
            "FileArchive" to FontAwesomeIcons.Regular.FileArchive,
            "FileAudio" to FontAwesomeIcons.Regular.FileAudio,
            "FileCode" to FontAwesomeIcons.Regular.FileCode,
            "FileExcel" to FontAwesomeIcons.Regular.FileExcel,
            "FileImage" to FontAwesomeIcons.Regular.FileImage,
            "FilePdf" to FontAwesomeIcons.Regular.FilePdf,
            "FilePowerpoint" to FontAwesomeIcons.Regular.FilePowerpoint,
            "FileVideo" to FontAwesomeIcons.Regular.FileVideo,
            "FileWord" to FontAwesomeIcons.Regular.FileWord,
            "Flag" to FontAwesomeIcons.Regular.Flag,
            "Flushed" to FontAwesomeIcons.Regular.Flushed,
            "Folder" to FontAwesomeIcons.Regular.Folder,
            "FolderOpen" to FontAwesomeIcons.Regular.FolderOpen,
            "FontAwesomeLogoFull" to FontAwesomeIcons.Regular.FontAwesomeLogoFull,
            "Frown" to FontAwesomeIcons.Regular.Frown,
            "FrownOpen" to FontAwesomeIcons.Regular.FrownOpen,
            "Futbol" to FontAwesomeIcons.Regular.Futbol,
            "Gem" to FontAwesomeIcons.Regular.Gem,
            "Grimace" to FontAwesomeIcons.Regular.Grimace,
            "Grin" to FontAwesomeIcons.Regular.Grin,
            "GrinAlt" to FontAwesomeIcons.Regular.GrinAlt,
            "GrinBeam" to FontAwesomeIcons.Regular.GrinBeam,
            "GrinBeamSweat" to FontAwesomeIcons.Regular.GrinBeamSweat,
            "GrinHearts" to FontAwesomeIcons.Regular.GrinHearts,
            "GrinSquint" to FontAwesomeIcons.Regular.GrinSquint,
            "GrinSquintTears" to FontAwesomeIcons.Regular.GrinSquintTears,
            "GrinStars" to FontAwesomeIcons.Regular.GrinStars,
            "GrinTears" to FontAwesomeIcons.Regular.GrinTears,
            "GrinTongue" to FontAwesomeIcons.Regular.GrinTongue,
            "GrinTongueSquint" to FontAwesomeIcons.Regular.GrinTongueSquint,
            "GrinTongueWink" to FontAwesomeIcons.Regular.GrinTongueWink,
            "GrinWink" to FontAwesomeIcons.Regular.GrinWink,
            "HandLizard" to FontAwesomeIcons.Regular.HandLizard,
            "HandPaper" to FontAwesomeIcons.Regular.HandPaper,
            "HandPeace" to FontAwesomeIcons.Regular.HandPeace,
            "HandPointDown" to FontAwesomeIcons.Regular.HandPointDown,
            "HandPointLeft" to FontAwesomeIcons.Regular.HandPointLeft,
            "HandPointRight" to FontAwesomeIcons.Regular.HandPointRight,
            "HandPointUp" to FontAwesomeIcons.Regular.HandPointUp,
            "HandPointer" to FontAwesomeIcons.Regular.HandPointer,
            "HandRock" to FontAwesomeIcons.Regular.HandRock,
            "HandScissors" to FontAwesomeIcons.Regular.HandScissors,
            "HandSpock" to FontAwesomeIcons.Regular.HandSpock,
            "Handshake" to FontAwesomeIcons.Regular.Handshake,
            "Hdd" to FontAwesomeIcons.Regular.Hdd,
            "Heart" to FontAwesomeIcons.Regular.Heart,
            "Hospital" to FontAwesomeIcons.Regular.Hospital,
            "Hourglass" to FontAwesomeIcons.Regular.Hourglass,
            "IdBadge" to FontAwesomeIcons.Regular.IdBadge,
            "IdCard" to FontAwesomeIcons.Regular.IdCard,
            "Image" to FontAwesomeIcons.Regular.Image,
            "Images" to FontAwesomeIcons.Regular.Images,
            "Keyboard" to FontAwesomeIcons.Regular.Keyboard,
            "Kiss" to FontAwesomeIcons.Regular.Kiss,
            "KissBeam" to FontAwesomeIcons.Regular.KissBeam,
            "KissWinkHeart" to FontAwesomeIcons.Regular.KissWinkHeart,
            "Laugh" to FontAwesomeIcons.Regular.Laugh,
            "LaughBeam" to FontAwesomeIcons.Regular.LaughBeam,
            "LaughSquint" to FontAwesomeIcons.Regular.LaughSquint,
            "LaughWink" to FontAwesomeIcons.Regular.LaughWink,
            "Lemon" to FontAwesomeIcons.Regular.Lemon,
            "LifeRing" to FontAwesomeIcons.Regular.LifeRing,
            "Lightbulb" to FontAwesomeIcons.Regular.Lightbulb,
            "ListAlt" to FontAwesomeIcons.Regular.ListAlt,
            "Map" to FontAwesomeIcons.Regular.Map,
            "Meh" to FontAwesomeIcons.Regular.Meh,
            "MehBlank" to FontAwesomeIcons.Regular.MehBlank,
            "MehRollingEyes" to FontAwesomeIcons.Regular.MehRollingEyes,
            "MinusSquare" to FontAwesomeIcons.Regular.MinusSquare,
            "MoneyBillAlt" to FontAwesomeIcons.Regular.MoneyBillAlt,
            "Moon" to FontAwesomeIcons.Regular.Moon,
            "Newspaper" to FontAwesomeIcons.Regular.Newspaper,
            "ObjectGroup" to FontAwesomeIcons.Regular.ObjectGroup,
            "ObjectUngroup" to FontAwesomeIcons.Regular.ObjectUngroup,
            "PaperPlane" to FontAwesomeIcons.Regular.PaperPlane,
            "PauseCircle" to FontAwesomeIcons.Regular.PauseCircle,
            "PlayCircle" to FontAwesomeIcons.Regular.PlayCircle,
            "PlusSquare" to FontAwesomeIcons.Regular.PlusSquare,
            "QuestionCircle" to FontAwesomeIcons.Regular.QuestionCircle,
            "Registered" to FontAwesomeIcons.Regular.Registered,
            "SadCry" to FontAwesomeIcons.Regular.SadCry,
            "SadTear" to FontAwesomeIcons.Regular.SadTear,
            "Save" to FontAwesomeIcons.Regular.Save,
            "ShareSquare" to FontAwesomeIcons.Regular.ShareSquare,
            "Smile" to FontAwesomeIcons.Regular.Smile,
            "SmileBeam" to FontAwesomeIcons.Regular.SmileBeam,
            "SmileWink" to FontAwesomeIcons.Regular.SmileWink,
            "Snowflake" to FontAwesomeIcons.Regular.Snowflake,
            "Square" to FontAwesomeIcons.Regular.Square,
            "Star" to FontAwesomeIcons.Regular.Star,
            "StarHalf" to FontAwesomeIcons.Regular.StarHalf,
            "StickyNote" to FontAwesomeIcons.Regular.StickyNote,
            "StopCircle" to FontAwesomeIcons.Regular.StopCircle,
            "Sun" to FontAwesomeIcons.Regular.Sun,
            "Surprise" to FontAwesomeIcons.Regular.Surprise,
            "ThumbsDown" to FontAwesomeIcons.Regular.ThumbsDown,
            "ThumbsUp" to FontAwesomeIcons.Regular.ThumbsUp,
            "TimesCircle" to FontAwesomeIcons.Regular.TimesCircle,
            "Tired" to FontAwesomeIcons.Regular.Tired,
            "TrashAlt" to FontAwesomeIcons.Regular.TrashAlt,
            "User" to FontAwesomeIcons.Regular.User,
            "UserCircle" to FontAwesomeIcons.Regular.UserCircle,
            "WindowClose" to FontAwesomeIcons.Regular.WindowClose,
            "WindowMaximize" to FontAwesomeIcons.Regular.WindowMaximize,
            "WindowMinimize" to FontAwesomeIcons.Regular.WindowMinimize,
            "WindowRestore" to FontAwesomeIcons.Regular.WindowRestore,
        )
    }

    val solidIcons: Map<String, ImageVector> by lazy {
        mapOf(
            "Ad" to FontAwesomeIcons.Solid.Ad,
            "AddressBook" to FontAwesomeIcons.Solid.AddressBook,
            "AddressCard" to FontAwesomeIcons.Solid.AddressCard,
            "Adjust" to FontAwesomeIcons.Solid.Adjust,
            "AirFreshener" to FontAwesomeIcons.Solid.AirFreshener,
            "AlignCenter" to FontAwesomeIcons.Solid.AlignCenter,
            "AlignJustify" to FontAwesomeIcons.Solid.AlignJustify,
            "AlignLeft" to FontAwesomeIcons.Solid.AlignLeft,
            "AlignRight" to FontAwesomeIcons.Solid.AlignRight,
            "Allergies" to FontAwesomeIcons.Solid.Allergies,
            "Ambulance" to FontAwesomeIcons.Solid.Ambulance,
            "AmericanSignLanguageInterpreting" to FontAwesomeIcons.Solid.AmericanSignLanguageInterpreting,
            "Anchor" to FontAwesomeIcons.Solid.Anchor,
            "AngleDoubleDown" to FontAwesomeIcons.Solid.AngleDoubleDown,
            "AngleDoubleLeft" to FontAwesomeIcons.Solid.AngleDoubleLeft,
            "AngleDoubleRight" to FontAwesomeIcons.Solid.AngleDoubleRight,
            "AngleDoubleUp" to FontAwesomeIcons.Solid.AngleDoubleUp,
            "AngleDown" to FontAwesomeIcons.Solid.AngleDown,
            "AngleLeft" to FontAwesomeIcons.Solid.AngleLeft,
            "AngleRight" to FontAwesomeIcons.Solid.AngleRight,
            "AngleUp" to FontAwesomeIcons.Solid.AngleUp,
            "Angry" to FontAwesomeIcons.Solid.Angry,
            "Ankh" to FontAwesomeIcons.Solid.Ankh,
            "AppleAlt" to FontAwesomeIcons.Solid.AppleAlt,
            "Archive" to FontAwesomeIcons.Solid.Archive,
            "Archway" to FontAwesomeIcons.Solid.Archway,
            "ArrowAltCircleDown" to FontAwesomeIcons.Solid.ArrowAltCircleDown,
            "ArrowAltCircleLeft" to FontAwesomeIcons.Solid.ArrowAltCircleLeft,
            "ArrowAltCircleRight" to FontAwesomeIcons.Solid.ArrowAltCircleRight,
            "ArrowAltCircleUp" to FontAwesomeIcons.Solid.ArrowAltCircleUp,
            "ArrowCircleDown" to FontAwesomeIcons.Solid.ArrowCircleDown,
            "ArrowCircleLeft" to FontAwesomeIcons.Solid.ArrowCircleLeft,
            "ArrowCircleRight" to FontAwesomeIcons.Solid.ArrowCircleRight,
            "ArrowCircleUp" to FontAwesomeIcons.Solid.ArrowCircleUp,
            "ArrowDown" to FontAwesomeIcons.Solid.ArrowDown,
            "ArrowLeft" to FontAwesomeIcons.Solid.ArrowLeft,
            "ArrowRight" to FontAwesomeIcons.Solid.ArrowRight,
            "ArrowUp" to FontAwesomeIcons.Solid.ArrowUp,
            "ArrowsAlt" to FontAwesomeIcons.Solid.ArrowsAlt,
            "ArrowsAltH" to FontAwesomeIcons.Solid.ArrowsAltH,
            "ArrowsAltV" to FontAwesomeIcons.Solid.ArrowsAltV,
            "AssistiveListeningSystems" to FontAwesomeIcons.Solid.AssistiveListeningSystems,
            "Asterisk" to FontAwesomeIcons.Solid.Asterisk,
            "At" to FontAwesomeIcons.Solid.At,
            "Atlas" to FontAwesomeIcons.Solid.Atlas,
            "Atom" to FontAwesomeIcons.Solid.Atom,
            "AudioDescription" to FontAwesomeIcons.Solid.AudioDescription,
            "Award" to FontAwesomeIcons.Solid.Award,
            "Baby" to FontAwesomeIcons.Solid.Baby,
            "BabyCarriage" to FontAwesomeIcons.Solid.BabyCarriage,
            "Backspace" to FontAwesomeIcons.Solid.Backspace,
            "Backward" to FontAwesomeIcons.Solid.Backward,
            "Bacon" to FontAwesomeIcons.Solid.Bacon,
            "Bacteria" to FontAwesomeIcons.Solid.Bacteria,
            "Bacterium" to FontAwesomeIcons.Solid.Bacterium,
            "Bahai" to FontAwesomeIcons.Solid.Bahai,
            "BalanceScale" to FontAwesomeIcons.Solid.BalanceScale,
            "BalanceScaleLeft" to FontAwesomeIcons.Solid.BalanceScaleLeft,
            "BalanceScaleRight" to FontAwesomeIcons.Solid.BalanceScaleRight,
            "Ban" to FontAwesomeIcons.Solid.Ban,
            "BandAid" to FontAwesomeIcons.Solid.BandAid,
            "Barcode" to FontAwesomeIcons.Solid.Barcode,
            "Bars" to FontAwesomeIcons.Solid.Bars,
            "BaseballBall" to FontAwesomeIcons.Solid.BaseballBall,
            "BasketballBall" to FontAwesomeIcons.Solid.BasketballBall,
            "Bath" to FontAwesomeIcons.Solid.Bath,
            "BatteryEmpty" to FontAwesomeIcons.Solid.BatteryEmpty,
            "BatteryFull" to FontAwesomeIcons.Solid.BatteryFull,
            "BatteryHalf" to FontAwesomeIcons.Solid.BatteryHalf,
            "BatteryQuarter" to FontAwesomeIcons.Solid.BatteryQuarter,
            "BatteryThreeQuarters" to FontAwesomeIcons.Solid.BatteryThreeQuarters,
            "Bed" to FontAwesomeIcons.Solid.Bed,
            "Beer" to FontAwesomeIcons.Solid.Beer,
            "Bell" to FontAwesomeIcons.Solid.Bell,
            "BellSlash" to FontAwesomeIcons.Solid.BellSlash,
            "BezierCurve" to FontAwesomeIcons.Solid.BezierCurve,
            "Bible" to FontAwesomeIcons.Solid.Bible,
            "Bicycle" to FontAwesomeIcons.Solid.Bicycle,
            "Biking" to FontAwesomeIcons.Solid.Biking,
            "Binoculars" to FontAwesomeIcons.Solid.Binoculars,
            "Biohazard" to FontAwesomeIcons.Solid.Biohazard,
            "BirthdayCake" to FontAwesomeIcons.Solid.BirthdayCake,
            "Blender" to FontAwesomeIcons.Solid.Blender,
            "BlenderPhone" to FontAwesomeIcons.Solid.BlenderPhone,
            "Blind" to FontAwesomeIcons.Solid.Blind,
            "Blog" to FontAwesomeIcons.Solid.Blog,
            "Bold" to FontAwesomeIcons.Solid.Bold,
            "Bolt" to FontAwesomeIcons.Solid.Bolt,
            "Bomb" to FontAwesomeIcons.Solid.Bomb,
            "Bone" to FontAwesomeIcons.Solid.Bone,
            "Bong" to FontAwesomeIcons.Solid.Bong,
            "Book" to FontAwesomeIcons.Solid.Book,
            "BookDead" to FontAwesomeIcons.Solid.BookDead,
            "BookMedical" to FontAwesomeIcons.Solid.BookMedical,
            "BookOpen" to FontAwesomeIcons.Solid.BookOpen,
            "BookReader" to FontAwesomeIcons.Solid.BookReader,
            "Bookmark" to FontAwesomeIcons.Solid.Bookmark,
            "BorderAll" to FontAwesomeIcons.Solid.BorderAll,
            "BorderNone" to FontAwesomeIcons.Solid.BorderNone,
            "BorderStyle" to FontAwesomeIcons.Solid.BorderStyle,
            "BowlingBall" to FontAwesomeIcons.Solid.BowlingBall,
            "Box" to FontAwesomeIcons.Solid.Box,
            "BoxOpen" to FontAwesomeIcons.Solid.BoxOpen,
            "BoxTissue" to FontAwesomeIcons.Solid.BoxTissue,
            "Boxes" to FontAwesomeIcons.Solid.Boxes,
            "Braille" to FontAwesomeIcons.Solid.Braille,
            "Brain" to FontAwesomeIcons.Solid.Brain,
            "BreadSlice" to FontAwesomeIcons.Solid.BreadSlice,
            "Briefcase" to FontAwesomeIcons.Solid.Briefcase,
            "BriefcaseMedical" to FontAwesomeIcons.Solid.BriefcaseMedical,
            "BroadcastTower" to FontAwesomeIcons.Solid.BroadcastTower,
            "Broom" to FontAwesomeIcons.Solid.Broom,
            "Brush" to FontAwesomeIcons.Solid.Brush,
            "Bug" to FontAwesomeIcons.Solid.Bug,
            "Building" to FontAwesomeIcons.Solid.Building,
            "Bullhorn" to FontAwesomeIcons.Solid.Bullhorn,
            "Bullseye" to FontAwesomeIcons.Solid.Bullseye,
            "Burn" to FontAwesomeIcons.Solid.Burn,
            "Bus" to FontAwesomeIcons.Solid.Bus,
            "BusAlt" to FontAwesomeIcons.Solid.BusAlt,
            "BusinessTime" to FontAwesomeIcons.Solid.BusinessTime,
            "Calculator" to FontAwesomeIcons.Solid.Calculator,
            "Calendar" to FontAwesomeIcons.Solid.Calendar,
            "CalendarAlt" to FontAwesomeIcons.Solid.CalendarAlt,
            "CalendarCheck" to FontAwesomeIcons.Solid.CalendarCheck,
            "CalendarDay" to FontAwesomeIcons.Solid.CalendarDay,
            "CalendarMinus" to FontAwesomeIcons.Solid.CalendarMinus,
            "CalendarPlus" to FontAwesomeIcons.Solid.CalendarPlus,
            "CalendarTimes" to FontAwesomeIcons.Solid.CalendarTimes,
            "CalendarWeek" to FontAwesomeIcons.Solid.CalendarWeek,
            "Camera" to FontAwesomeIcons.Solid.Camera,
            "CameraRetro" to FontAwesomeIcons.Solid.CameraRetro,
            "Campground" to FontAwesomeIcons.Solid.Campground,
            "CandyCane" to FontAwesomeIcons.Solid.CandyCane,
            "Cannabis" to FontAwesomeIcons.Solid.Cannabis,
            "Capsules" to FontAwesomeIcons.Solid.Capsules,
            "Car" to FontAwesomeIcons.Solid.Car,
            "CarAlt" to FontAwesomeIcons.Solid.CarAlt,
            "CarBattery" to FontAwesomeIcons.Solid.CarBattery,
            "CarCrash" to FontAwesomeIcons.Solid.CarCrash,
            "CarSide" to FontAwesomeIcons.Solid.CarSide,
            "Caravan" to FontAwesomeIcons.Solid.Caravan,
            "CaretDown" to FontAwesomeIcons.Solid.CaretDown,
            "CaretLeft" to FontAwesomeIcons.Solid.CaretLeft,
            "CaretRight" to FontAwesomeIcons.Solid.CaretRight,
            "CaretSquareDown" to FontAwesomeIcons.Solid.CaretSquareDown,
            "CaretSquareLeft" to FontAwesomeIcons.Solid.CaretSquareLeft,
            "CaretSquareRight" to FontAwesomeIcons.Solid.CaretSquareRight,
            "CaretSquareUp" to FontAwesomeIcons.Solid.CaretSquareUp,
            "CaretUp" to FontAwesomeIcons.Solid.CaretUp,
            "Carrot" to FontAwesomeIcons.Solid.Carrot,
            "CartArrowDown" to FontAwesomeIcons.Solid.CartArrowDown,
            "CartPlus" to FontAwesomeIcons.Solid.CartPlus,
            "CashRegister" to FontAwesomeIcons.Solid.CashRegister,
            "Cat" to FontAwesomeIcons.Solid.Cat,
            "Certificate" to FontAwesomeIcons.Solid.Certificate,
            "Chair" to FontAwesomeIcons.Solid.Chair,
            "Chalkboard" to FontAwesomeIcons.Solid.Chalkboard,
            "ChalkboardTeacher" to FontAwesomeIcons.Solid.ChalkboardTeacher,
            "ChargingStation" to FontAwesomeIcons.Solid.ChargingStation,
            "ChartArea" to FontAwesomeIcons.Solid.ChartArea,
            "ChartBar" to FontAwesomeIcons.Solid.ChartBar,
            "ChartLine" to FontAwesomeIcons.Solid.ChartLine,
            "ChartPie" to FontAwesomeIcons.Solid.ChartPie,
            "Check" to FontAwesomeIcons.Solid.Check,
            "CheckCircle" to FontAwesomeIcons.Solid.CheckCircle,
            "CheckDouble" to FontAwesomeIcons.Solid.CheckDouble,
            "CheckSquare" to FontAwesomeIcons.Solid.CheckSquare,
            "Cheese" to FontAwesomeIcons.Solid.Cheese,
            "Chess" to FontAwesomeIcons.Solid.Chess,
            "ChessBishop" to FontAwesomeIcons.Solid.ChessBishop,
            "ChessBoard" to FontAwesomeIcons.Solid.ChessBoard,
            "ChessKing" to FontAwesomeIcons.Solid.ChessKing,
            "ChessKnight" to FontAwesomeIcons.Solid.ChessKnight,
            "ChessPawn" to FontAwesomeIcons.Solid.ChessPawn,
            "ChessQueen" to FontAwesomeIcons.Solid.ChessQueen,
            "ChessRook" to FontAwesomeIcons.Solid.ChessRook,
            "ChevronCircleDown" to FontAwesomeIcons.Solid.ChevronCircleDown,
            "ChevronCircleLeft" to FontAwesomeIcons.Solid.ChevronCircleLeft,
            "ChevronCircleRight" to FontAwesomeIcons.Solid.ChevronCircleRight,
            "ChevronCircleUp" to FontAwesomeIcons.Solid.ChevronCircleUp,
            "ChevronDown" to FontAwesomeIcons.Solid.ChevronDown,
            "ChevronLeft" to FontAwesomeIcons.Solid.ChevronLeft,
            "ChevronRight" to FontAwesomeIcons.Solid.ChevronRight,
            "ChevronUp" to FontAwesomeIcons.Solid.ChevronUp,
            "Child" to FontAwesomeIcons.Solid.Child,
            "Church" to FontAwesomeIcons.Solid.Church,
            "Circle" to FontAwesomeIcons.Solid.Circle,
            "CircleNotch" to FontAwesomeIcons.Solid.CircleNotch,
            "City" to FontAwesomeIcons.Solid.City,
            "ClinicMedical" to FontAwesomeIcons.Solid.ClinicMedical,
            "Clipboard" to FontAwesomeIcons.Solid.Clipboard,
            "ClipboardCheck" to FontAwesomeIcons.Solid.ClipboardCheck,
            "ClipboardList" to FontAwesomeIcons.Solid.ClipboardList,
            "Clock" to FontAwesomeIcons.Solid.Clock,
            "Clone" to FontAwesomeIcons.Solid.Clone,
            "ClosedCaptioning" to FontAwesomeIcons.Solid.ClosedCaptioning,
            "Cloud" to FontAwesomeIcons.Solid.Cloud,
            "CloudDownloadAlt" to FontAwesomeIcons.Solid.CloudDownloadAlt,
            "CloudMeatball" to FontAwesomeIcons.Solid.CloudMeatball,
            "CloudMoon" to FontAwesomeIcons.Solid.CloudMoon,
            "CloudMoonRain" to FontAwesomeIcons.Solid.CloudMoonRain,
            "CloudRain" to FontAwesomeIcons.Solid.CloudRain,
            "CloudShowersHeavy" to FontAwesomeIcons.Solid.CloudShowersHeavy,
            "CloudSun" to FontAwesomeIcons.Solid.CloudSun,
            "CloudSunRain" to FontAwesomeIcons.Solid.CloudSunRain,
            "CloudUploadAlt" to FontAwesomeIcons.Solid.CloudUploadAlt,
            "Cocktail" to FontAwesomeIcons.Solid.Cocktail,
            "Code" to FontAwesomeIcons.Solid.Code,
            "CodeBranch" to FontAwesomeIcons.Solid.CodeBranch,
            "Coffee" to FontAwesomeIcons.Solid.Coffee,
            "Cog" to FontAwesomeIcons.Solid.Cog,
            "Cogs" to FontAwesomeIcons.Solid.Cogs,
            "Coins" to FontAwesomeIcons.Solid.Coins,
            "Columns" to FontAwesomeIcons.Solid.Columns,
            "Comment" to FontAwesomeIcons.Solid.Comment,
            "CommentAlt" to FontAwesomeIcons.Solid.CommentAlt,
            "CommentDollar" to FontAwesomeIcons.Solid.CommentDollar,
            "CommentDots" to FontAwesomeIcons.Solid.CommentDots,
            "CommentMedical" to FontAwesomeIcons.Solid.CommentMedical,
            "CommentSlash" to FontAwesomeIcons.Solid.CommentSlash,
            "Comments" to FontAwesomeIcons.Solid.Comments,
            "CommentsDollar" to FontAwesomeIcons.Solid.CommentsDollar,
            "CompactDisc" to FontAwesomeIcons.Solid.CompactDisc,
            "Compass" to FontAwesomeIcons.Solid.Compass,
            "Compress" to FontAwesomeIcons.Solid.Compress,
            "CompressAlt" to FontAwesomeIcons.Solid.CompressAlt,
            "CompressArrowsAlt" to FontAwesomeIcons.Solid.CompressArrowsAlt,
            "ConciergeBell" to FontAwesomeIcons.Solid.ConciergeBell,
            "Cookie" to FontAwesomeIcons.Solid.Cookie,
            "CookieBite" to FontAwesomeIcons.Solid.CookieBite,
            "Copy" to FontAwesomeIcons.Solid.Copy,
            "Copyright" to FontAwesomeIcons.Solid.Copyright,
            "Couch" to FontAwesomeIcons.Solid.Couch,
            "CreditCard" to FontAwesomeIcons.Solid.CreditCard,
            "Crop" to FontAwesomeIcons.Solid.Crop,
            "CropAlt" to FontAwesomeIcons.Solid.CropAlt,
            "Cross" to FontAwesomeIcons.Solid.Cross,
            "Crosshairs" to FontAwesomeIcons.Solid.Crosshairs,
            "Crow" to FontAwesomeIcons.Solid.Crow,
            "Crown" to FontAwesomeIcons.Solid.Crown,
            "Crutch" to FontAwesomeIcons.Solid.Crutch,
            "Cube" to FontAwesomeIcons.Solid.Cube,
            "Cubes" to FontAwesomeIcons.Solid.Cubes,
            "Cut" to FontAwesomeIcons.Solid.Cut,
            "Database" to FontAwesomeIcons.Solid.Database,
            "Deaf" to FontAwesomeIcons.Solid.Deaf,
            "Democrat" to FontAwesomeIcons.Solid.Democrat,
            "Desktop" to FontAwesomeIcons.Solid.Desktop,
            "Dharmachakra" to FontAwesomeIcons.Solid.Dharmachakra,
            "Diagnoses" to FontAwesomeIcons.Solid.Diagnoses,
            "Dice" to FontAwesomeIcons.Solid.Dice,
            "DiceD20" to FontAwesomeIcons.Solid.DiceD20,
            "DiceD6" to FontAwesomeIcons.Solid.DiceD6,
            "DiceFive" to FontAwesomeIcons.Solid.DiceFive,
            "DiceFour" to FontAwesomeIcons.Solid.DiceFour,
            "DiceOne" to FontAwesomeIcons.Solid.DiceOne,
            "DiceSix" to FontAwesomeIcons.Solid.DiceSix,
            "DiceThree" to FontAwesomeIcons.Solid.DiceThree,
            "DiceTwo" to FontAwesomeIcons.Solid.DiceTwo,
            "DigitalTachograph" to FontAwesomeIcons.Solid.DigitalTachograph,
            "Directions" to FontAwesomeIcons.Solid.Directions,
            "Disease" to FontAwesomeIcons.Solid.Disease,
            "Divide" to FontAwesomeIcons.Solid.Divide,
            "Dizzy" to FontAwesomeIcons.Solid.Dizzy,
            "Dna" to FontAwesomeIcons.Solid.Dna,
            "Dog" to FontAwesomeIcons.Solid.Dog,
            "DollarSign" to FontAwesomeIcons.Solid.DollarSign,
            "Dolly" to FontAwesomeIcons.Solid.Dolly,
            "DollyFlatbed" to FontAwesomeIcons.Solid.DollyFlatbed,
            "Donate" to FontAwesomeIcons.Solid.Donate,
            "DoorClosed" to FontAwesomeIcons.Solid.DoorClosed,
            "DoorOpen" to FontAwesomeIcons.Solid.DoorOpen,
            "DotCircle" to FontAwesomeIcons.Solid.DotCircle,
            "Dove" to FontAwesomeIcons.Solid.Dove,
            "Download" to FontAwesomeIcons.Solid.Download,
            "DraftingCompass" to FontAwesomeIcons.Solid.DraftingCompass,
            "Dragon" to FontAwesomeIcons.Solid.Dragon,
            "DrawPolygon" to FontAwesomeIcons.Solid.DrawPolygon,
            "Drum" to FontAwesomeIcons.Solid.Drum,
            "DrumSteelpan" to FontAwesomeIcons.Solid.DrumSteelpan,
            "DrumstickBite" to FontAwesomeIcons.Solid.DrumstickBite,
            "Dumbbell" to FontAwesomeIcons.Solid.Dumbbell,
            "Dumpster" to FontAwesomeIcons.Solid.Dumpster,
            "DumpsterFire" to FontAwesomeIcons.Solid.DumpsterFire,
            "Dungeon" to FontAwesomeIcons.Solid.Dungeon,
            "Edit" to FontAwesomeIcons.Solid.Edit,
            "Egg" to FontAwesomeIcons.Solid.Egg,
            "Eject" to FontAwesomeIcons.Solid.Eject,
            "EllipsisH" to FontAwesomeIcons.Solid.EllipsisH,
            "EllipsisV" to FontAwesomeIcons.Solid.EllipsisV,
            "Envelope" to FontAwesomeIcons.Solid.Envelope,
            "EnvelopeOpen" to FontAwesomeIcons.Solid.EnvelopeOpen,
            "EnvelopeOpenText" to FontAwesomeIcons.Solid.EnvelopeOpenText,
            "EnvelopeSquare" to FontAwesomeIcons.Solid.EnvelopeSquare,
            "Equals" to FontAwesomeIcons.Solid.Equals,
            "Eraser" to FontAwesomeIcons.Solid.Eraser,
            "Ethernet" to FontAwesomeIcons.Solid.Ethernet,
            "EuroSign" to FontAwesomeIcons.Solid.EuroSign,
            "ExchangeAlt" to FontAwesomeIcons.Solid.ExchangeAlt,
            "Exclamation" to FontAwesomeIcons.Solid.Exclamation,
            "ExclamationCircle" to FontAwesomeIcons.Solid.ExclamationCircle,
            "ExclamationTriangle" to FontAwesomeIcons.Solid.ExclamationTriangle,
            "Expand" to FontAwesomeIcons.Solid.Expand,
            "ExpandAlt" to FontAwesomeIcons.Solid.ExpandAlt,
            "ExpandArrowsAlt" to FontAwesomeIcons.Solid.ExpandArrowsAlt,
            "ExternalLinkAlt" to FontAwesomeIcons.Solid.ExternalLinkAlt,
            "ExternalLinkSquareAlt" to FontAwesomeIcons.Solid.ExternalLinkSquareAlt,
            "Eye" to FontAwesomeIcons.Solid.Eye,
            "EyeDropper" to FontAwesomeIcons.Solid.EyeDropper,
            "EyeSlash" to FontAwesomeIcons.Solid.EyeSlash,
            "Fan" to FontAwesomeIcons.Solid.Fan,
            "FastBackward" to FontAwesomeIcons.Solid.FastBackward,
            "FastForward" to FontAwesomeIcons.Solid.FastForward,
            "Faucet" to FontAwesomeIcons.Solid.Faucet,
            "Fax" to FontAwesomeIcons.Solid.Fax,
            "Feather" to FontAwesomeIcons.Solid.Feather,
            "FeatherAlt" to FontAwesomeIcons.Solid.FeatherAlt,
            "Female" to FontAwesomeIcons.Solid.Female,
            "FighterJet" to FontAwesomeIcons.Solid.FighterJet,
            "File" to FontAwesomeIcons.Solid.File,
            "FileAlt" to FontAwesomeIcons.Solid.FileAlt,
            "FileArchive" to FontAwesomeIcons.Solid.FileArchive,
            "FileAudio" to FontAwesomeIcons.Solid.FileAudio,
            "FileCode" to FontAwesomeIcons.Solid.FileCode,
            "FileContract" to FontAwesomeIcons.Solid.FileContract,
            "FileCsv" to FontAwesomeIcons.Solid.FileCsv,
            "FileDownload" to FontAwesomeIcons.Solid.FileDownload,
            "FileExcel" to FontAwesomeIcons.Solid.FileExcel,
            "FileExport" to FontAwesomeIcons.Solid.FileExport,
            "FileImage" to FontAwesomeIcons.Solid.FileImage,
            "FileImport" to FontAwesomeIcons.Solid.FileImport,
            "FileInvoice" to FontAwesomeIcons.Solid.FileInvoice,
            "FileInvoiceDollar" to FontAwesomeIcons.Solid.FileInvoiceDollar,
            "FileMedical" to FontAwesomeIcons.Solid.FileMedical,
            "FileMedicalAlt" to FontAwesomeIcons.Solid.FileMedicalAlt,
            "FilePdf" to FontAwesomeIcons.Solid.FilePdf,
            "FilePowerpoint" to FontAwesomeIcons.Solid.FilePowerpoint,
            "FilePrescription" to FontAwesomeIcons.Solid.FilePrescription,
            "FileSignature" to FontAwesomeIcons.Solid.FileSignature,
            "FileUpload" to FontAwesomeIcons.Solid.FileUpload,
            "FileVideo" to FontAwesomeIcons.Solid.FileVideo,
            "FileWord" to FontAwesomeIcons.Solid.FileWord,
            "Fill" to FontAwesomeIcons.Solid.Fill,
            "FillDrip" to FontAwesomeIcons.Solid.FillDrip,
            "Film" to FontAwesomeIcons.Solid.Film,
            "Filter" to FontAwesomeIcons.Solid.Filter,
            "Fingerprint" to FontAwesomeIcons.Solid.Fingerprint,
            "Fire" to FontAwesomeIcons.Solid.Fire,
            "FireAlt" to FontAwesomeIcons.Solid.FireAlt,
            "FireExtinguisher" to FontAwesomeIcons.Solid.FireExtinguisher,
            "FirstAid" to FontAwesomeIcons.Solid.FirstAid,
            "Fish" to FontAwesomeIcons.Solid.Fish,
            "FistRaised" to FontAwesomeIcons.Solid.FistRaised,
            "Flag" to FontAwesomeIcons.Solid.Flag,
            "FlagCheckered" to FontAwesomeIcons.Solid.FlagCheckered,
            "FlagUsa" to FontAwesomeIcons.Solid.FlagUsa,
            "Flask" to FontAwesomeIcons.Solid.Flask,
            "Flushed" to FontAwesomeIcons.Solid.Flushed,
            "Folder" to FontAwesomeIcons.Solid.Folder,
            "FolderMinus" to FontAwesomeIcons.Solid.FolderMinus,
            "FolderOpen" to FontAwesomeIcons.Solid.FolderOpen,
            "FolderPlus" to FontAwesomeIcons.Solid.FolderPlus,
            "Font" to FontAwesomeIcons.Solid.Font,
            "FontAwesomeLogoFull" to FontAwesomeIcons.Solid.FontAwesomeLogoFull,
            "FootballBall" to FontAwesomeIcons.Solid.FootballBall,
            "Forward" to FontAwesomeIcons.Solid.Forward,
            "Frog" to FontAwesomeIcons.Solid.Frog,
            "Frown" to FontAwesomeIcons.Solid.Frown,
            "FrownOpen" to FontAwesomeIcons.Solid.FrownOpen,
            "FunnelDollar" to FontAwesomeIcons.Solid.FunnelDollar,
            "Futbol" to FontAwesomeIcons.Solid.Futbol,
            "Gamepad" to FontAwesomeIcons.Solid.Gamepad,
            "GasPump" to FontAwesomeIcons.Solid.GasPump,
            "Gavel" to FontAwesomeIcons.Solid.Gavel,
            "Gem" to FontAwesomeIcons.Solid.Gem,
            "Genderless" to FontAwesomeIcons.Solid.Genderless,
            "Ghost" to FontAwesomeIcons.Solid.Ghost,
            "Gift" to FontAwesomeIcons.Solid.Gift,
            "Gifts" to FontAwesomeIcons.Solid.Gifts,
            "GlassCheers" to FontAwesomeIcons.Solid.GlassCheers,
            "GlassMartini" to FontAwesomeIcons.Solid.GlassMartini,
            "GlassMartiniAlt" to FontAwesomeIcons.Solid.GlassMartiniAlt,
            "GlassWhiskey" to FontAwesomeIcons.Solid.GlassWhiskey,
            "Glasses" to FontAwesomeIcons.Solid.Glasses,
            "Globe" to FontAwesomeIcons.Solid.Globe,
            "GlobeAfrica" to FontAwesomeIcons.Solid.GlobeAfrica,
            "GlobeAmericas" to FontAwesomeIcons.Solid.GlobeAmericas,
            "GlobeAsia" to FontAwesomeIcons.Solid.GlobeAsia,
            "GlobeEurope" to FontAwesomeIcons.Solid.GlobeEurope,
            "GolfBall" to FontAwesomeIcons.Solid.GolfBall,
            "Gopuram" to FontAwesomeIcons.Solid.Gopuram,
            "GraduationCap" to FontAwesomeIcons.Solid.GraduationCap,
            "GreaterThan" to FontAwesomeIcons.Solid.GreaterThan,
            "GreaterThanEqual" to FontAwesomeIcons.Solid.GreaterThanEqual,
            "Grimace" to FontAwesomeIcons.Solid.Grimace,
            "Grin" to FontAwesomeIcons.Solid.Grin,
            "GrinAlt" to FontAwesomeIcons.Solid.GrinAlt,
            "GrinBeam" to FontAwesomeIcons.Solid.GrinBeam,
            "GrinBeamSweat" to FontAwesomeIcons.Solid.GrinBeamSweat,
            "GrinHearts" to FontAwesomeIcons.Solid.GrinHearts,
            "GrinSquint" to FontAwesomeIcons.Solid.GrinSquint,
            "GrinSquintTears" to FontAwesomeIcons.Solid.GrinSquintTears,
            "GrinStars" to FontAwesomeIcons.Solid.GrinStars,
            "GrinTears" to FontAwesomeIcons.Solid.GrinTears,
            "GrinTongue" to FontAwesomeIcons.Solid.GrinTongue,
            "GrinTongueSquint" to FontAwesomeIcons.Solid.GrinTongueSquint,
            "GrinTongueWink" to FontAwesomeIcons.Solid.GrinTongueWink,
            "GrinWink" to FontAwesomeIcons.Solid.GrinWink,
            "GripHorizontal" to FontAwesomeIcons.Solid.GripHorizontal,
            "GripLines" to FontAwesomeIcons.Solid.GripLines,
            "GripLinesVertical" to FontAwesomeIcons.Solid.GripLinesVertical,
            "GripVertical" to FontAwesomeIcons.Solid.GripVertical,
            "Guitar" to FontAwesomeIcons.Solid.Guitar,
            "HSquare" to FontAwesomeIcons.Solid.HSquare,
            "Hamburger" to FontAwesomeIcons.Solid.Hamburger,
            "Hammer" to FontAwesomeIcons.Solid.Hammer,
            "Hamsa" to FontAwesomeIcons.Solid.Hamsa,
            "HandHolding" to FontAwesomeIcons.Solid.HandHolding,
            "HandHoldingHeart" to FontAwesomeIcons.Solid.HandHoldingHeart,
            "HandHoldingMedical" to FontAwesomeIcons.Solid.HandHoldingMedical,
            "HandHoldingUsd" to FontAwesomeIcons.Solid.HandHoldingUsd,
            "HandHoldingWater" to FontAwesomeIcons.Solid.HandHoldingWater,
            "HandLizard" to FontAwesomeIcons.Solid.HandLizard,
            "HandMiddleFinger" to FontAwesomeIcons.Solid.HandMiddleFinger,
            "HandPaper" to FontAwesomeIcons.Solid.HandPaper,
            "HandPeace" to FontAwesomeIcons.Solid.HandPeace,
            "HandPointDown" to FontAwesomeIcons.Solid.HandPointDown,
            "HandPointLeft" to FontAwesomeIcons.Solid.HandPointLeft,
            "HandPointRight" to FontAwesomeIcons.Solid.HandPointRight,
            "HandPointUp" to FontAwesomeIcons.Solid.HandPointUp,
            "HandPointer" to FontAwesomeIcons.Solid.HandPointer,
            "HandRock" to FontAwesomeIcons.Solid.HandRock,
            "HandScissors" to FontAwesomeIcons.Solid.HandScissors,
            "HandSparkles" to FontAwesomeIcons.Solid.HandSparkles,
            "HandSpock" to FontAwesomeIcons.Solid.HandSpock,
            "Hands" to FontAwesomeIcons.Solid.Hands,
            "HandsHelping" to FontAwesomeIcons.Solid.HandsHelping,
            "HandsWash" to FontAwesomeIcons.Solid.HandsWash,
            "Handshake" to FontAwesomeIcons.Solid.Handshake,
            "HandshakeAltSlash" to FontAwesomeIcons.Solid.HandshakeAltSlash,
            "HandshakeSlash" to FontAwesomeIcons.Solid.HandshakeSlash,
            "Hanukiah" to FontAwesomeIcons.Solid.Hanukiah,
            "HardHat" to FontAwesomeIcons.Solid.HardHat,
            "Hashtag" to FontAwesomeIcons.Solid.Hashtag,
            "HatCowboy" to FontAwesomeIcons.Solid.HatCowboy,
            "HatCowboySide" to FontAwesomeIcons.Solid.HatCowboySide,
            "HatWizard" to FontAwesomeIcons.Solid.HatWizard,
            "Hdd" to FontAwesomeIcons.Solid.Hdd,
            "HeadSideCough" to FontAwesomeIcons.Solid.HeadSideCough,
            "HeadSideCoughSlash" to FontAwesomeIcons.Solid.HeadSideCoughSlash,
            "HeadSideMask" to FontAwesomeIcons.Solid.HeadSideMask,
            "HeadSideVirus" to FontAwesomeIcons.Solid.HeadSideVirus,
            "Heading" to FontAwesomeIcons.Solid.Heading,
            "Headphones" to FontAwesomeIcons.Solid.Headphones,
            "HeadphonesAlt" to FontAwesomeIcons.Solid.HeadphonesAlt,
            "Headset" to FontAwesomeIcons.Solid.Headset,
            "Heart" to FontAwesomeIcons.Solid.Heart,
            "HeartBroken" to FontAwesomeIcons.Solid.HeartBroken,
            "Heartbeat" to FontAwesomeIcons.Solid.Heartbeat,
            "Helicopter" to FontAwesomeIcons.Solid.Helicopter,
            "Highlighter" to FontAwesomeIcons.Solid.Highlighter,
            "Hiking" to FontAwesomeIcons.Solid.Hiking,
            "Hippo" to FontAwesomeIcons.Solid.Hippo,
            "History" to FontAwesomeIcons.Solid.History,
            "HockeyPuck" to FontAwesomeIcons.Solid.HockeyPuck,
            "HollyBerry" to FontAwesomeIcons.Solid.HollyBerry,
            "Home" to FontAwesomeIcons.Solid.Home,
            "Horse" to FontAwesomeIcons.Solid.Horse,
            "HorseHead" to FontAwesomeIcons.Solid.HorseHead,
            "Hospital" to FontAwesomeIcons.Solid.Hospital,
            "HospitalAlt" to FontAwesomeIcons.Solid.HospitalAlt,
            "HospitalSymbol" to FontAwesomeIcons.Solid.HospitalSymbol,
            "HospitalUser" to FontAwesomeIcons.Solid.HospitalUser,
            "HotTub" to FontAwesomeIcons.Solid.HotTub,
            "Hotdog" to FontAwesomeIcons.Solid.Hotdog,
            "Hotel" to FontAwesomeIcons.Solid.Hotel,
            "Hourglass" to FontAwesomeIcons.Solid.Hourglass,
            "HourglassEnd" to FontAwesomeIcons.Solid.HourglassEnd,
            "HourglassHalf" to FontAwesomeIcons.Solid.HourglassHalf,
            "HourglassStart" to FontAwesomeIcons.Solid.HourglassStart,
            "HouseDamage" to FontAwesomeIcons.Solid.HouseDamage,
            "HouseUser" to FontAwesomeIcons.Solid.HouseUser,
            "Hryvnia" to FontAwesomeIcons.Solid.Hryvnia,
            "ICursor" to FontAwesomeIcons.Solid.ICursor,
            "IceCream" to FontAwesomeIcons.Solid.IceCream,
            "Icicles" to FontAwesomeIcons.Solid.Icicles,
            "Icons" to FontAwesomeIcons.Solid.Icons,
            "IdBadge" to FontAwesomeIcons.Solid.IdBadge,
            "IdCard" to FontAwesomeIcons.Solid.IdCard,
            "IdCardAlt" to FontAwesomeIcons.Solid.IdCardAlt,
            "Igloo" to FontAwesomeIcons.Solid.Igloo,
            "Image" to FontAwesomeIcons.Solid.Image,
            "Images" to FontAwesomeIcons.Solid.Images,
            "Inbox" to FontAwesomeIcons.Solid.Inbox,
            "Indent" to FontAwesomeIcons.Solid.Indent,
            "Industry" to FontAwesomeIcons.Solid.Industry,
            "Infinity" to FontAwesomeIcons.Solid.Infinity,
            "Info" to FontAwesomeIcons.Solid.Info,
            "InfoCircle" to FontAwesomeIcons.Solid.InfoCircle,
            "Italic" to FontAwesomeIcons.Solid.Italic,
            "Jedi" to FontAwesomeIcons.Solid.Jedi,
            "Joint" to FontAwesomeIcons.Solid.Joint,
            "JournalWhills" to FontAwesomeIcons.Solid.JournalWhills,
            "Kaaba" to FontAwesomeIcons.Solid.Kaaba,
            "Key" to FontAwesomeIcons.Solid.Key,
            "Keyboard" to FontAwesomeIcons.Solid.Keyboard,
            "Khanda" to FontAwesomeIcons.Solid.Khanda,
            "Kiss" to FontAwesomeIcons.Solid.Kiss,
            "KissBeam" to FontAwesomeIcons.Solid.KissBeam,
            "KissWinkHeart" to FontAwesomeIcons.Solid.KissWinkHeart,
            "KiwiBird" to FontAwesomeIcons.Solid.KiwiBird,
            "Landmark" to FontAwesomeIcons.Solid.Landmark,
            "Language" to FontAwesomeIcons.Solid.Language,
            "Laptop" to FontAwesomeIcons.Solid.Laptop,
            "LaptopCode" to FontAwesomeIcons.Solid.LaptopCode,
            "LaptopHouse" to FontAwesomeIcons.Solid.LaptopHouse,
            "LaptopMedical" to FontAwesomeIcons.Solid.LaptopMedical,
            "Laugh" to FontAwesomeIcons.Solid.Laugh,
            "LaughBeam" to FontAwesomeIcons.Solid.LaughBeam,
            "LaughSquint" to FontAwesomeIcons.Solid.LaughSquint,
            "LaughWink" to FontAwesomeIcons.Solid.LaughWink,
            "LayerGroup" to FontAwesomeIcons.Solid.LayerGroup,
            "Leaf" to FontAwesomeIcons.Solid.Leaf,
            "Lemon" to FontAwesomeIcons.Solid.Lemon,
            "LessThan" to FontAwesomeIcons.Solid.LessThan,
            "LessThanEqual" to FontAwesomeIcons.Solid.LessThanEqual,
            "LevelDownAlt" to FontAwesomeIcons.Solid.LevelDownAlt,
            "LevelUpAlt" to FontAwesomeIcons.Solid.LevelUpAlt,
            "LifeRing" to FontAwesomeIcons.Solid.LifeRing,
            "Lightbulb" to FontAwesomeIcons.Solid.Lightbulb,
            "Link" to FontAwesomeIcons.Solid.Link,
            "LiraSign" to FontAwesomeIcons.Solid.LiraSign,
            "List" to FontAwesomeIcons.Solid.List,
            "ListAlt" to FontAwesomeIcons.Solid.ListAlt,
            "ListOl" to FontAwesomeIcons.Solid.ListOl,
            "ListUl" to FontAwesomeIcons.Solid.ListUl,
            "LocationArrow" to FontAwesomeIcons.Solid.LocationArrow,
            "Lock" to FontAwesomeIcons.Solid.Lock,
            "LockOpen" to FontAwesomeIcons.Solid.LockOpen,
            "LongArrowAltDown" to FontAwesomeIcons.Solid.LongArrowAltDown,
            "LongArrowAltLeft" to FontAwesomeIcons.Solid.LongArrowAltLeft,
            "LongArrowAltRight" to FontAwesomeIcons.Solid.LongArrowAltRight,
            "LongArrowAltUp" to FontAwesomeIcons.Solid.LongArrowAltUp,
            "LowVision" to FontAwesomeIcons.Solid.LowVision,
            "LuggageCart" to FontAwesomeIcons.Solid.LuggageCart,
            "Lungs" to FontAwesomeIcons.Solid.Lungs,
            "LungsVirus" to FontAwesomeIcons.Solid.LungsVirus,
            "Magic" to FontAwesomeIcons.Solid.Magic,
            "Magnet" to FontAwesomeIcons.Solid.Magnet,
            "MailBulk" to FontAwesomeIcons.Solid.MailBulk,
            "Male" to FontAwesomeIcons.Solid.Male,
            "Map" to FontAwesomeIcons.Solid.Map,
            "MapMarked" to FontAwesomeIcons.Solid.MapMarked,
            "MapMarkedAlt" to FontAwesomeIcons.Solid.MapMarkedAlt,
            "MapMarker" to FontAwesomeIcons.Solid.MapMarker,
            "MapMarkerAlt" to FontAwesomeIcons.Solid.MapMarkerAlt,
            "MapPin" to FontAwesomeIcons.Solid.MapPin,
            "MapSigns" to FontAwesomeIcons.Solid.MapSigns,
            "Marker" to FontAwesomeIcons.Solid.Marker,
            "Mars" to FontAwesomeIcons.Solid.Mars,
            "MarsDouble" to FontAwesomeIcons.Solid.MarsDouble,
            "MarsStroke" to FontAwesomeIcons.Solid.MarsStroke,
            "MarsStrokeH" to FontAwesomeIcons.Solid.MarsStrokeH,
            "MarsStrokeV" to FontAwesomeIcons.Solid.MarsStrokeV,
            "Mask" to FontAwesomeIcons.Solid.Mask,
            "Medal" to FontAwesomeIcons.Solid.Medal,
            "Medkit" to FontAwesomeIcons.Solid.Medkit,
            "Meh" to FontAwesomeIcons.Solid.Meh,
            "MehBlank" to FontAwesomeIcons.Solid.MehBlank,
            "MehRollingEyes" to FontAwesomeIcons.Solid.MehRollingEyes,
            "Memory" to FontAwesomeIcons.Solid.Memory,
            "Menorah" to FontAwesomeIcons.Solid.Menorah,
            "Mercury" to FontAwesomeIcons.Solid.Mercury,
            "Meteor" to FontAwesomeIcons.Solid.Meteor,
            "Microchip" to FontAwesomeIcons.Solid.Microchip,
            "Microphone" to FontAwesomeIcons.Solid.Microphone,
            "MicrophoneAlt" to FontAwesomeIcons.Solid.MicrophoneAlt,
            "MicrophoneAltSlash" to FontAwesomeIcons.Solid.MicrophoneAltSlash,
            "MicrophoneSlash" to FontAwesomeIcons.Solid.MicrophoneSlash,
            "Microscope" to FontAwesomeIcons.Solid.Microscope,
            "Minus" to FontAwesomeIcons.Solid.Minus,
            "MinusCircle" to FontAwesomeIcons.Solid.MinusCircle,
            "MinusSquare" to FontAwesomeIcons.Solid.MinusSquare,
            "Mitten" to FontAwesomeIcons.Solid.Mitten,
            "Mobile" to FontAwesomeIcons.Solid.Mobile,
            "MobileAlt" to FontAwesomeIcons.Solid.MobileAlt,
            "MoneyBill" to FontAwesomeIcons.Solid.MoneyBill,
            "MoneyBillAlt" to FontAwesomeIcons.Solid.MoneyBillAlt,
            "MoneyBillWave" to FontAwesomeIcons.Solid.MoneyBillWave,
            "MoneyBillWaveAlt" to FontAwesomeIcons.Solid.MoneyBillWaveAlt,
            "MoneyCheck" to FontAwesomeIcons.Solid.MoneyCheck,
            "MoneyCheckAlt" to FontAwesomeIcons.Solid.MoneyCheckAlt,
            "Monument" to FontAwesomeIcons.Solid.Monument,
            "Moon" to FontAwesomeIcons.Solid.Moon,
            "MortarPestle" to FontAwesomeIcons.Solid.MortarPestle,
            "Mosque" to FontAwesomeIcons.Solid.Mosque,
            "Motorcycle" to FontAwesomeIcons.Solid.Motorcycle,
            "Mountain" to FontAwesomeIcons.Solid.Mountain,
            "Mouse" to FontAwesomeIcons.Solid.Mouse,
            "MousePointer" to FontAwesomeIcons.Solid.MousePointer,
            "MugHot" to FontAwesomeIcons.Solid.MugHot,
            "Music" to FontAwesomeIcons.Solid.Music,
            "NetworkWired" to FontAwesomeIcons.Solid.NetworkWired,
            "Neuter" to FontAwesomeIcons.Solid.Neuter,
            "Newspaper" to FontAwesomeIcons.Solid.Newspaper,
            "NotEqual" to FontAwesomeIcons.Solid.NotEqual,
            "NotesMedical" to FontAwesomeIcons.Solid.NotesMedical,
            "ObjectGroup" to FontAwesomeIcons.Solid.ObjectGroup,
            "ObjectUngroup" to FontAwesomeIcons.Solid.ObjectUngroup,
            "OilCan" to FontAwesomeIcons.Solid.OilCan,
            "Om" to FontAwesomeIcons.Solid.Om,
            "Otter" to FontAwesomeIcons.Solid.Otter,
            "Outdent" to FontAwesomeIcons.Solid.Outdent,
            "Pager" to FontAwesomeIcons.Solid.Pager,
            "PaintBrush" to FontAwesomeIcons.Solid.PaintBrush,
            "PaintRoller" to FontAwesomeIcons.Solid.PaintRoller,
            "Palette" to FontAwesomeIcons.Solid.Palette,
            "Pallet" to FontAwesomeIcons.Solid.Pallet,
            "PaperPlane" to FontAwesomeIcons.Solid.PaperPlane,
            "Paperclip" to FontAwesomeIcons.Solid.Paperclip,
            "ParachuteBox" to FontAwesomeIcons.Solid.ParachuteBox,
            "Paragraph" to FontAwesomeIcons.Solid.Paragraph,
            "Parking" to FontAwesomeIcons.Solid.Parking,
            "Passport" to FontAwesomeIcons.Solid.Passport,
            "Pastafarianism" to FontAwesomeIcons.Solid.Pastafarianism,
            "Paste" to FontAwesomeIcons.Solid.Paste,
            "Pause" to FontAwesomeIcons.Solid.Pause,
            "PauseCircle" to FontAwesomeIcons.Solid.PauseCircle,
            "Paw" to FontAwesomeIcons.Solid.Paw,
            "Peace" to FontAwesomeIcons.Solid.Peace,
            "Pen" to FontAwesomeIcons.Solid.Pen,
            "PenAlt" to FontAwesomeIcons.Solid.PenAlt,
            "PenFancy" to FontAwesomeIcons.Solid.PenFancy,
            "PenNib" to FontAwesomeIcons.Solid.PenNib,
            "PenSquare" to FontAwesomeIcons.Solid.PenSquare,
            "PencilAlt" to FontAwesomeIcons.Solid.PencilAlt,
            "PencilRuler" to FontAwesomeIcons.Solid.PencilRuler,
            "PeopleArrows" to FontAwesomeIcons.Solid.PeopleArrows,
            "PeopleCarry" to FontAwesomeIcons.Solid.PeopleCarry,
            "PepperHot" to FontAwesomeIcons.Solid.PepperHot,
            "Percent" to FontAwesomeIcons.Solid.Percent,
            "Percentage" to FontAwesomeIcons.Solid.Percentage,
            "PersonBooth" to FontAwesomeIcons.Solid.PersonBooth,
            "Phone" to FontAwesomeIcons.Solid.Phone,
            "PhoneAlt" to FontAwesomeIcons.Solid.PhoneAlt,
            "PhoneSlash" to FontAwesomeIcons.Solid.PhoneSlash,
            "PhoneSquare" to FontAwesomeIcons.Solid.PhoneSquare,
            "PhoneSquareAlt" to FontAwesomeIcons.Solid.PhoneSquareAlt,
            "PhoneVolume" to FontAwesomeIcons.Solid.PhoneVolume,
            "PhotoVideo" to FontAwesomeIcons.Solid.PhotoVideo,
            "PiggyBank" to FontAwesomeIcons.Solid.PiggyBank,
            "Pills" to FontAwesomeIcons.Solid.Pills,
            "PizzaSlice" to FontAwesomeIcons.Solid.PizzaSlice,
            "PlaceOfWorship" to FontAwesomeIcons.Solid.PlaceOfWorship,
            "Plane" to FontAwesomeIcons.Solid.Plane,
            "PlaneArrival" to FontAwesomeIcons.Solid.PlaneArrival,
            "PlaneDeparture" to FontAwesomeIcons.Solid.PlaneDeparture,
            "PlaneSlash" to FontAwesomeIcons.Solid.PlaneSlash,
            "Play" to FontAwesomeIcons.Solid.Play,
            "PlayCircle" to FontAwesomeIcons.Solid.PlayCircle,
            "Plug" to FontAwesomeIcons.Solid.Plug,
            "Plus" to FontAwesomeIcons.Solid.Plus,
            "PlusCircle" to FontAwesomeIcons.Solid.PlusCircle,
            "PlusSquare" to FontAwesomeIcons.Solid.PlusSquare,
            "Podcast" to FontAwesomeIcons.Solid.Podcast,
            "Poll" to FontAwesomeIcons.Solid.Poll,
            "PollH" to FontAwesomeIcons.Solid.PollH,
            "Poo" to FontAwesomeIcons.Solid.Poo,
            "PooStorm" to FontAwesomeIcons.Solid.PooStorm,
            "Poop" to FontAwesomeIcons.Solid.Poop,
            "Portrait" to FontAwesomeIcons.Solid.Portrait,
            "PoundSign" to FontAwesomeIcons.Solid.PoundSign,
            "PowerOff" to FontAwesomeIcons.Solid.PowerOff,
            "Pray" to FontAwesomeIcons.Solid.Pray,
            "PrayingHands" to FontAwesomeIcons.Solid.PrayingHands,
            "Prescription" to FontAwesomeIcons.Solid.Prescription,
            "PrescriptionBottle" to FontAwesomeIcons.Solid.PrescriptionBottle,
            "PrescriptionBottleAlt" to FontAwesomeIcons.Solid.PrescriptionBottleAlt,
            "Print" to FontAwesomeIcons.Solid.Print,
            "Procedures" to FontAwesomeIcons.Solid.Procedures,
            "ProjectDiagram" to FontAwesomeIcons.Solid.ProjectDiagram,
            "PumpMedical" to FontAwesomeIcons.Solid.PumpMedical,
            "PumpSoap" to FontAwesomeIcons.Solid.PumpSoap,
            "PuzzlePiece" to FontAwesomeIcons.Solid.PuzzlePiece,
            "Qrcode" to FontAwesomeIcons.Solid.Qrcode,
            "Question" to FontAwesomeIcons.Solid.Question,
            "QuestionCircle" to FontAwesomeIcons.Solid.QuestionCircle,
            "Quidditch" to FontAwesomeIcons.Solid.Quidditch,
            "QuoteLeft" to FontAwesomeIcons.Solid.QuoteLeft,
            "QuoteRight" to FontAwesomeIcons.Solid.QuoteRight,
            "Quran" to FontAwesomeIcons.Solid.Quran,
            "Radiation" to FontAwesomeIcons.Solid.Radiation,
            "RadiationAlt" to FontAwesomeIcons.Solid.RadiationAlt,
            "Rainbow" to FontAwesomeIcons.Solid.Rainbow,
            "Random" to FontAwesomeIcons.Solid.Random,
            "Receipt" to FontAwesomeIcons.Solid.Receipt,
            "RecordVinyl" to FontAwesomeIcons.Solid.RecordVinyl,
            "Recycle" to FontAwesomeIcons.Solid.Recycle,
            "Redo" to FontAwesomeIcons.Solid.Redo,
            "RedoAlt" to FontAwesomeIcons.Solid.RedoAlt,
            "Registered" to FontAwesomeIcons.Solid.Registered,
            "RemoveFormat" to FontAwesomeIcons.Solid.RemoveFormat,
            "Reply" to FontAwesomeIcons.Solid.Reply,
            "ReplyAll" to FontAwesomeIcons.Solid.ReplyAll,
            "Republican" to FontAwesomeIcons.Solid.Republican,
            "Restroom" to FontAwesomeIcons.Solid.Restroom,
            "Retweet" to FontAwesomeIcons.Solid.Retweet,
            "Ribbon" to FontAwesomeIcons.Solid.Ribbon,
            "Ring" to FontAwesomeIcons.Solid.Ring,
            "Road" to FontAwesomeIcons.Solid.Road,
            "Robot" to FontAwesomeIcons.Solid.Robot,
            "Rocket" to FontAwesomeIcons.Solid.Rocket,
            "Route" to FontAwesomeIcons.Solid.Route,
            "Rss" to FontAwesomeIcons.Solid.Rss,
            "RssSquare" to FontAwesomeIcons.Solid.RssSquare,
            "RubleSign" to FontAwesomeIcons.Solid.RubleSign,
            "Ruler" to FontAwesomeIcons.Solid.Ruler,
            "RulerCombined" to FontAwesomeIcons.Solid.RulerCombined,
            "RulerHorizontal" to FontAwesomeIcons.Solid.RulerHorizontal,
            "RulerVertical" to FontAwesomeIcons.Solid.RulerVertical,
            "Running" to FontAwesomeIcons.Solid.Running,
            "RupeeSign" to FontAwesomeIcons.Solid.RupeeSign,
            "SadCry" to FontAwesomeIcons.Solid.SadCry,
            "SadTear" to FontAwesomeIcons.Solid.SadTear,
            "Satellite" to FontAwesomeIcons.Solid.Satellite,
            "SatelliteDish" to FontAwesomeIcons.Solid.SatelliteDish,
            "Save" to FontAwesomeIcons.Solid.Save,
            "School" to FontAwesomeIcons.Solid.School,
            "Screwdriver" to FontAwesomeIcons.Solid.Screwdriver,
            "Scroll" to FontAwesomeIcons.Solid.Scroll,
            "SdCard" to FontAwesomeIcons.Solid.SdCard,
            "Search" to FontAwesomeIcons.Solid.Search,
            "SearchDollar" to FontAwesomeIcons.Solid.SearchDollar,
            "SearchLocation" to FontAwesomeIcons.Solid.SearchLocation,
            "SearchMinus" to FontAwesomeIcons.Solid.SearchMinus,
            "SearchPlus" to FontAwesomeIcons.Solid.SearchPlus,
            "Seedling" to FontAwesomeIcons.Solid.Seedling,
            "Server" to FontAwesomeIcons.Solid.Server,
            "Shapes" to FontAwesomeIcons.Solid.Shapes,
            "Share" to FontAwesomeIcons.Solid.Share,
            "ShareAlt" to FontAwesomeIcons.Solid.ShareAlt,
            "ShareAltSquare" to FontAwesomeIcons.Solid.ShareAltSquare,
            "ShareSquare" to FontAwesomeIcons.Solid.ShareSquare,
            "ShekelSign" to FontAwesomeIcons.Solid.ShekelSign,
            "ShieldAlt" to FontAwesomeIcons.Solid.ShieldAlt,
            "ShieldVirus" to FontAwesomeIcons.Solid.ShieldVirus,
            "Ship" to FontAwesomeIcons.Solid.Ship,
            "ShippingFast" to FontAwesomeIcons.Solid.ShippingFast,
            "ShoePrints" to FontAwesomeIcons.Solid.ShoePrints,
            "ShoppingBag" to FontAwesomeIcons.Solid.ShoppingBag,
            "ShoppingBasket" to FontAwesomeIcons.Solid.ShoppingBasket,
            "ShoppingCart" to FontAwesomeIcons.Solid.ShoppingCart,
            "Shower" to FontAwesomeIcons.Solid.Shower,
            "ShuttleVan" to FontAwesomeIcons.Solid.ShuttleVan,
            "Sign" to FontAwesomeIcons.Solid.Sign,
            "SignInAlt" to FontAwesomeIcons.Solid.SignInAlt,
            "SignLanguage" to FontAwesomeIcons.Solid.SignLanguage,
            "SignOutAlt" to FontAwesomeIcons.Solid.SignOutAlt,
            "Signal" to FontAwesomeIcons.Solid.Signal,
            "Signature" to FontAwesomeIcons.Solid.Signature,
            "SimCard" to FontAwesomeIcons.Solid.SimCard,
            "Sink" to FontAwesomeIcons.Solid.Sink,
            "Sitemap" to FontAwesomeIcons.Solid.Sitemap,
            "Skating" to FontAwesomeIcons.Solid.Skating,
            "Skiing" to FontAwesomeIcons.Solid.Skiing,
            "SkiingNordic" to FontAwesomeIcons.Solid.SkiingNordic,
            "Skull" to FontAwesomeIcons.Solid.Skull,
            "SkullCrossbones" to FontAwesomeIcons.Solid.SkullCrossbones,
            "Slash" to FontAwesomeIcons.Solid.Slash,
            "Sleigh" to FontAwesomeIcons.Solid.Sleigh,
            "SlidersH" to FontAwesomeIcons.Solid.SlidersH,
            "Smile" to FontAwesomeIcons.Solid.Smile,
            "SmileBeam" to FontAwesomeIcons.Solid.SmileBeam,
            "SmileWink" to FontAwesomeIcons.Solid.SmileWink,
            "Smog" to FontAwesomeIcons.Solid.Smog,
            "Smoking" to FontAwesomeIcons.Solid.Smoking,
            "SmokingBan" to FontAwesomeIcons.Solid.SmokingBan,
            "Sms" to FontAwesomeIcons.Solid.Sms,
            "Snowboarding" to FontAwesomeIcons.Solid.Snowboarding,
            "Snowflake" to FontAwesomeIcons.Solid.Snowflake,
            "Snowman" to FontAwesomeIcons.Solid.Snowman,
            "Snowplow" to FontAwesomeIcons.Solid.Snowplow,
            "Soap" to FontAwesomeIcons.Solid.Soap,
            "Socks" to FontAwesomeIcons.Solid.Socks,
            "SolarPanel" to FontAwesomeIcons.Solid.SolarPanel,
            "Sort" to FontAwesomeIcons.Solid.Sort,
            "SortAlphaDown" to FontAwesomeIcons.Solid.SortAlphaDown,
            "SortAlphaDownAlt" to FontAwesomeIcons.Solid.SortAlphaDownAlt,
            "SortAlphaUp" to FontAwesomeIcons.Solid.SortAlphaUp,
            "SortAlphaUpAlt" to FontAwesomeIcons.Solid.SortAlphaUpAlt,
            "SortAmountDown" to FontAwesomeIcons.Solid.SortAmountDown,
            "SortAmountDownAlt" to FontAwesomeIcons.Solid.SortAmountDownAlt,
            "SortAmountUp" to FontAwesomeIcons.Solid.SortAmountUp,
            "SortAmountUpAlt" to FontAwesomeIcons.Solid.SortAmountUpAlt,
            "SortDown" to FontAwesomeIcons.Solid.SortDown,
            "SortNumericDown" to FontAwesomeIcons.Solid.SortNumericDown,
            "SortNumericDownAlt" to FontAwesomeIcons.Solid.SortNumericDownAlt,
            "SortNumericUp" to FontAwesomeIcons.Solid.SortNumericUp,
            "SortNumericUpAlt" to FontAwesomeIcons.Solid.SortNumericUpAlt,
            "SortUp" to FontAwesomeIcons.Solid.SortUp,
            "Spa" to FontAwesomeIcons.Solid.Spa,
            "SpaceShuttle" to FontAwesomeIcons.Solid.SpaceShuttle,
            "SpellCheck" to FontAwesomeIcons.Solid.SpellCheck,
            "Spider" to FontAwesomeIcons.Solid.Spider,
            "Spinner" to FontAwesomeIcons.Solid.Spinner,
            "Splotch" to FontAwesomeIcons.Solid.Splotch,
            "SprayCan" to FontAwesomeIcons.Solid.SprayCan,
            "Square" to FontAwesomeIcons.Solid.Square,
            "SquareFull" to FontAwesomeIcons.Solid.SquareFull,
            "SquareRootAlt" to FontAwesomeIcons.Solid.SquareRootAlt,
            "Stamp" to FontAwesomeIcons.Solid.Stamp,
            "Star" to FontAwesomeIcons.Solid.Star,
            "StarAndCrescent" to FontAwesomeIcons.Solid.StarAndCrescent,
            "StarHalf" to FontAwesomeIcons.Solid.StarHalf,
            "StarHalfAlt" to FontAwesomeIcons.Solid.StarHalfAlt,
            "StarOfDavid" to FontAwesomeIcons.Solid.StarOfDavid,
            "StarOfLife" to FontAwesomeIcons.Solid.StarOfLife,
            "StepBackward" to FontAwesomeIcons.Solid.StepBackward,
            "StepForward" to FontAwesomeIcons.Solid.StepForward,
            "Stethoscope" to FontAwesomeIcons.Solid.Stethoscope,
            "StickyNote" to FontAwesomeIcons.Solid.StickyNote,
            "Stop" to FontAwesomeIcons.Solid.Stop,
            "StopCircle" to FontAwesomeIcons.Solid.StopCircle,
            "Stopwatch" to FontAwesomeIcons.Solid.Stopwatch,
            "Stopwatch20" to FontAwesomeIcons.Solid.Stopwatch20,
            "Store" to FontAwesomeIcons.Solid.Store,
            "StoreAlt" to FontAwesomeIcons.Solid.StoreAlt,
            "StoreAltSlash" to FontAwesomeIcons.Solid.StoreAltSlash,
            "StoreSlash" to FontAwesomeIcons.Solid.StoreSlash,
            "Stream" to FontAwesomeIcons.Solid.Stream,
            "StreetView" to FontAwesomeIcons.Solid.StreetView,
            "Strikethrough" to FontAwesomeIcons.Solid.Strikethrough,
            "Stroopwafel" to FontAwesomeIcons.Solid.Stroopwafel,
            "Subscript" to FontAwesomeIcons.Solid.Subscript,
            "Subway" to FontAwesomeIcons.Solid.Subway,
            "Suitcase" to FontAwesomeIcons.Solid.Suitcase,
            "SuitcaseRolling" to FontAwesomeIcons.Solid.SuitcaseRolling,
            "Sun" to FontAwesomeIcons.Solid.Sun,
            "Superscript" to FontAwesomeIcons.Solid.Superscript,
            "Surprise" to FontAwesomeIcons.Solid.Surprise,
            "Swatchbook" to FontAwesomeIcons.Solid.Swatchbook,
            "Swimmer" to FontAwesomeIcons.Solid.Swimmer,
            "SwimmingPool" to FontAwesomeIcons.Solid.SwimmingPool,
            "Synagogue" to FontAwesomeIcons.Solid.Synagogue,
            "Sync" to FontAwesomeIcons.Solid.Sync,
            "SyncAlt" to FontAwesomeIcons.Solid.SyncAlt,
            "Syringe" to FontAwesomeIcons.Solid.Syringe,
            "Table" to FontAwesomeIcons.Solid.Table,
            "TableTennis" to FontAwesomeIcons.Solid.TableTennis,
            "Tablet" to FontAwesomeIcons.Solid.Tablet,
            "TabletAlt" to FontAwesomeIcons.Solid.TabletAlt,
            "Tablets" to FontAwesomeIcons.Solid.Tablets,
            "TachometerAlt" to FontAwesomeIcons.Solid.TachometerAlt,
            "Tag" to FontAwesomeIcons.Solid.Tag,
            "Tags" to FontAwesomeIcons.Solid.Tags,
            "Tape" to FontAwesomeIcons.Solid.Tape,
            "Tasks" to FontAwesomeIcons.Solid.Tasks,
            "Taxi" to FontAwesomeIcons.Solid.Taxi,
            "Teeth" to FontAwesomeIcons.Solid.Teeth,
            "TeethOpen" to FontAwesomeIcons.Solid.TeethOpen,
            "TemperatureHigh" to FontAwesomeIcons.Solid.TemperatureHigh,
            "TemperatureLow" to FontAwesomeIcons.Solid.TemperatureLow,
            "Tenge" to FontAwesomeIcons.Solid.Tenge,
            "Terminal" to FontAwesomeIcons.Solid.Terminal,
            "TextHeight" to FontAwesomeIcons.Solid.TextHeight,
            "TextWidth" to FontAwesomeIcons.Solid.TextWidth,
            "Th" to FontAwesomeIcons.Solid.Th,
            "ThLarge" to FontAwesomeIcons.Solid.ThLarge,
            "ThList" to FontAwesomeIcons.Solid.ThList,
            "TheaterMasks" to FontAwesomeIcons.Solid.TheaterMasks,
            "Thermometer" to FontAwesomeIcons.Solid.Thermometer,
            "ThermometerEmpty" to FontAwesomeIcons.Solid.ThermometerEmpty,
            "ThermometerFull" to FontAwesomeIcons.Solid.ThermometerFull,
            "ThermometerHalf" to FontAwesomeIcons.Solid.ThermometerHalf,
            "ThermometerQuarter" to FontAwesomeIcons.Solid.ThermometerQuarter,
            "ThermometerThreeQuarters" to FontAwesomeIcons.Solid.ThermometerThreeQuarters,
            "ThumbsDown" to FontAwesomeIcons.Solid.ThumbsDown,
            "ThumbsUp" to FontAwesomeIcons.Solid.ThumbsUp,
            "Thumbtack" to FontAwesomeIcons.Solid.Thumbtack,
            "TicketAlt" to FontAwesomeIcons.Solid.TicketAlt,
            "Times" to FontAwesomeIcons.Solid.Times,
            "TimesCircle" to FontAwesomeIcons.Solid.TimesCircle,
            "Tint" to FontAwesomeIcons.Solid.Tint,
            "TintSlash" to FontAwesomeIcons.Solid.TintSlash,
            "Tired" to FontAwesomeIcons.Solid.Tired,
            "ToggleOff" to FontAwesomeIcons.Solid.ToggleOff,
            "ToggleOn" to FontAwesomeIcons.Solid.ToggleOn,
            "Toilet" to FontAwesomeIcons.Solid.Toilet,
            "ToiletPaper" to FontAwesomeIcons.Solid.ToiletPaper,
            "ToiletPaperSlash" to FontAwesomeIcons.Solid.ToiletPaperSlash,
            "Toolbox" to FontAwesomeIcons.Solid.Toolbox,
            "Tools" to FontAwesomeIcons.Solid.Tools,
            "Tooth" to FontAwesomeIcons.Solid.Tooth,
            "Torah" to FontAwesomeIcons.Solid.Torah,
            "ToriiGate" to FontAwesomeIcons.Solid.ToriiGate,
            "Tractor" to FontAwesomeIcons.Solid.Tractor,
            "Trademark" to FontAwesomeIcons.Solid.Trademark,
            "TrafficLight" to FontAwesomeIcons.Solid.TrafficLight,
            "Trailer" to FontAwesomeIcons.Solid.Trailer,
            "Train" to FontAwesomeIcons.Solid.Train,
            "Tram" to FontAwesomeIcons.Solid.Tram,
            "Transgender" to FontAwesomeIcons.Solid.Transgender,
            "TransgenderAlt" to FontAwesomeIcons.Solid.TransgenderAlt,
            "Trash" to FontAwesomeIcons.Solid.Trash,
            "TrashAlt" to FontAwesomeIcons.Solid.TrashAlt,
            "TrashRestore" to FontAwesomeIcons.Solid.TrashRestore,
            "TrashRestoreAlt" to FontAwesomeIcons.Solid.TrashRestoreAlt,
            "Tree" to FontAwesomeIcons.Solid.Tree,
            "Trophy" to FontAwesomeIcons.Solid.Trophy,
            "Truck" to FontAwesomeIcons.Solid.Truck,
            "TruckLoading" to FontAwesomeIcons.Solid.TruckLoading,
            "TruckMonster" to FontAwesomeIcons.Solid.TruckMonster,
            "TruckMoving" to FontAwesomeIcons.Solid.TruckMoving,
            "TruckPickup" to FontAwesomeIcons.Solid.TruckPickup,
            "Tshirt" to FontAwesomeIcons.Solid.Tshirt,
            "Tty" to FontAwesomeIcons.Solid.Tty,
            "Tv" to FontAwesomeIcons.Solid.Tv,
            "Umbrella" to FontAwesomeIcons.Solid.Umbrella,
            "UmbrellaBeach" to FontAwesomeIcons.Solid.UmbrellaBeach,
            "Underline" to FontAwesomeIcons.Solid.Underline,
            "Undo" to FontAwesomeIcons.Solid.Undo,
            "UndoAlt" to FontAwesomeIcons.Solid.UndoAlt,
            "UniversalAccess" to FontAwesomeIcons.Solid.UniversalAccess,
            "University" to FontAwesomeIcons.Solid.University,
            "Unlink" to FontAwesomeIcons.Solid.Unlink,
            "Unlock" to FontAwesomeIcons.Solid.Unlock,
            "UnlockAlt" to FontAwesomeIcons.Solid.UnlockAlt,
            "Upload" to FontAwesomeIcons.Solid.Upload,
            "User" to FontAwesomeIcons.Solid.User,
            "UserAlt" to FontAwesomeIcons.Solid.UserAlt,
            "UserAltSlash" to FontAwesomeIcons.Solid.UserAltSlash,
            "UserAstronaut" to FontAwesomeIcons.Solid.UserAstronaut,
            "UserCheck" to FontAwesomeIcons.Solid.UserCheck,
            "UserCircle" to FontAwesomeIcons.Solid.UserCircle,
            "UserClock" to FontAwesomeIcons.Solid.UserClock,
            "UserCog" to FontAwesomeIcons.Solid.UserCog,
            "UserEdit" to FontAwesomeIcons.Solid.UserEdit,
            "UserFriends" to FontAwesomeIcons.Solid.UserFriends,
            "UserGraduate" to FontAwesomeIcons.Solid.UserGraduate,
            "UserInjured" to FontAwesomeIcons.Solid.UserInjured,
            "UserLock" to FontAwesomeIcons.Solid.UserLock,
            "UserMd" to FontAwesomeIcons.Solid.UserMd,
            "UserMinus" to FontAwesomeIcons.Solid.UserMinus,
            "UserNinja" to FontAwesomeIcons.Solid.UserNinja,
            "UserNurse" to FontAwesomeIcons.Solid.UserNurse,
            "UserPlus" to FontAwesomeIcons.Solid.UserPlus,
            "UserSecret" to FontAwesomeIcons.Solid.UserSecret,
            "UserShield" to FontAwesomeIcons.Solid.UserShield,
            "UserSlash" to FontAwesomeIcons.Solid.UserSlash,
            "UserTag" to FontAwesomeIcons.Solid.UserTag,
            "UserTie" to FontAwesomeIcons.Solid.UserTie,
            "UserTimes" to FontAwesomeIcons.Solid.UserTimes,
            "Users" to FontAwesomeIcons.Solid.Users,
            "UsersCog" to FontAwesomeIcons.Solid.UsersCog,
            "UsersSlash" to FontAwesomeIcons.Solid.UsersSlash,
            "UtensilSpoon" to FontAwesomeIcons.Solid.UtensilSpoon,
            "Utensils" to FontAwesomeIcons.Solid.Utensils,
            "VectorSquare" to FontAwesomeIcons.Solid.VectorSquare,
            "Venus" to FontAwesomeIcons.Solid.Venus,
            "VenusDouble" to FontAwesomeIcons.Solid.VenusDouble,
            "VenusMars" to FontAwesomeIcons.Solid.VenusMars,
            "Vest" to FontAwesomeIcons.Solid.Vest,
            "VestPatches" to FontAwesomeIcons.Solid.VestPatches,
            "Vial" to FontAwesomeIcons.Solid.Vial,
            "Vials" to FontAwesomeIcons.Solid.Vials,
            "Video" to FontAwesomeIcons.Solid.Video,
            "VideoSlash" to FontAwesomeIcons.Solid.VideoSlash,
            "Vihara" to FontAwesomeIcons.Solid.Vihara,
            "Virus" to FontAwesomeIcons.Solid.Virus,
            "VirusSlash" to FontAwesomeIcons.Solid.VirusSlash,
            "Viruses" to FontAwesomeIcons.Solid.Viruses,
            "Voicemail" to FontAwesomeIcons.Solid.Voicemail,
            "VolleyballBall" to FontAwesomeIcons.Solid.VolleyballBall,
            "VolumeDown" to FontAwesomeIcons.Solid.VolumeDown,
            "VolumeMute" to FontAwesomeIcons.Solid.VolumeMute,
            "VolumeOff" to FontAwesomeIcons.Solid.VolumeOff,
            "VolumeUp" to FontAwesomeIcons.Solid.VolumeUp,
            "VoteYea" to FontAwesomeIcons.Solid.VoteYea,
            "VrCardboard" to FontAwesomeIcons.Solid.VrCardboard,
            "Walking" to FontAwesomeIcons.Solid.Walking,
            "Wallet" to FontAwesomeIcons.Solid.Wallet,
            "Warehouse" to FontAwesomeIcons.Solid.Warehouse,
            "Water" to FontAwesomeIcons.Solid.Water,
            "WaveSquare" to FontAwesomeIcons.Solid.WaveSquare,
            "Weight" to FontAwesomeIcons.Solid.Weight,
            "WeightHanging" to FontAwesomeIcons.Solid.WeightHanging,
            "Wheelchair" to FontAwesomeIcons.Solid.Wheelchair,
            "Wifi" to FontAwesomeIcons.Solid.Wifi,
            "Wind" to FontAwesomeIcons.Solid.Wind,
            "WindowClose" to FontAwesomeIcons.Solid.WindowClose,
            "WindowMaximize" to FontAwesomeIcons.Solid.WindowMaximize,
            "WindowMinimize" to FontAwesomeIcons.Solid.WindowMinimize,
            "WindowRestore" to FontAwesomeIcons.Solid.WindowRestore,
            "WineBottle" to FontAwesomeIcons.Solid.WineBottle,
            "WineGlass" to FontAwesomeIcons.Solid.WineGlass,
            "WineGlassAlt" to FontAwesomeIcons.Solid.WineGlassAlt,
            "WonSign" to FontAwesomeIcons.Solid.WonSign,
            "Wrench" to FontAwesomeIcons.Solid.Wrench,
            "XRay" to FontAwesomeIcons.Solid.XRay,
            "YenSign" to FontAwesomeIcons.Solid.YenSign,
            "YinYang" to FontAwesomeIcons.Solid.YinYang,
        )
    }

    val brandIcon: Map<String, ImageVector> by lazy {
        mapOf(
            "500px" to FontAwesomeIcons.Brands._500px,
            "AccessibleIcon" to FontAwesomeIcons.Brands.AccessibleIcon,
            "Accusoft" to FontAwesomeIcons.Brands.Accusoft,
            "AcquisitionsIncorporated" to FontAwesomeIcons.Brands.AcquisitionsIncorporated,
            "Adn" to FontAwesomeIcons.Brands.Adn,
            "Adversal" to FontAwesomeIcons.Brands.Adversal,
            "Affiliatetheme" to FontAwesomeIcons.Brands.Affiliatetheme,
            "Airbnb" to FontAwesomeIcons.Brands.Airbnb,
            "Algolia" to FontAwesomeIcons.Brands.Algolia,
            "Alipay" to FontAwesomeIcons.Brands.Alipay,
            "Amazon" to FontAwesomeIcons.Brands.Amazon,
            "AmazonPay" to FontAwesomeIcons.Brands.AmazonPay,
            "Amilia" to FontAwesomeIcons.Brands.Amilia,
            "Android" to FontAwesomeIcons.Brands.Android,
            "Angellist" to FontAwesomeIcons.Brands.Angellist,
            "Angrycreative" to FontAwesomeIcons.Brands.Angrycreative,
            "Angular" to FontAwesomeIcons.Brands.Angular,
            "AppStore" to FontAwesomeIcons.Brands.AppStore,
            "AppStoreIos" to FontAwesomeIcons.Brands.AppStoreIos,
            "Apper" to FontAwesomeIcons.Brands.Apper,
            "Apple" to FontAwesomeIcons.Brands.Apple,
            "ApplePay" to FontAwesomeIcons.Brands.ApplePay,
            "Artstation" to FontAwesomeIcons.Brands.Artstation,
            "Asymmetrik" to FontAwesomeIcons.Brands.Asymmetrik,
            "Atlassian" to FontAwesomeIcons.Brands.Atlassian,
            "Audible" to FontAwesomeIcons.Brands.Audible,
            "Autoprefixer" to FontAwesomeIcons.Brands.Autoprefixer,
            "Avianex" to FontAwesomeIcons.Brands.Avianex,
            "Aviato" to FontAwesomeIcons.Brands.Aviato,
            "Aws" to FontAwesomeIcons.Brands.Aws,
            "Bandcamp" to FontAwesomeIcons.Brands.Bandcamp,
            "BattleNet" to FontAwesomeIcons.Brands.BattleNet,
            "Behance" to FontAwesomeIcons.Brands.Behance,
            "BehanceSquare" to FontAwesomeIcons.Brands.BehanceSquare,
            "Bimobject" to FontAwesomeIcons.Brands.Bimobject,
            "Bitbucket" to FontAwesomeIcons.Brands.Bitbucket,
            "Bitcoin" to FontAwesomeIcons.Brands.Bitcoin,
            "Bity" to FontAwesomeIcons.Brands.Bity,
            "BlackTie" to FontAwesomeIcons.Brands.BlackTie,
            "Blackberry" to FontAwesomeIcons.Brands.Blackberry,
            "Blogger" to FontAwesomeIcons.Brands.Blogger,
            "BloggerB" to FontAwesomeIcons.Brands.BloggerB,
            "Bluetooth" to FontAwesomeIcons.Brands.Bluetooth,
            "BluetoothB" to FontAwesomeIcons.Brands.BluetoothB,
            "Bootstrap" to FontAwesomeIcons.Brands.Bootstrap,
            "Btc" to FontAwesomeIcons.Brands.Btc,
            "Buffer" to FontAwesomeIcons.Brands.Buffer,
            "Buromobelexperte" to FontAwesomeIcons.Brands.Buromobelexperte,
            "BuyNLarge" to FontAwesomeIcons.Brands.BuyNLarge,
            "Buysellads" to FontAwesomeIcons.Brands.Buysellads,
            "CanadianMapleLeaf" to FontAwesomeIcons.Brands.CanadianMapleLeaf,
            "CcAmazonPay" to FontAwesomeIcons.Brands.CcAmazonPay,
            "CcAmex" to FontAwesomeIcons.Brands.CcAmex,
            "CcApplePay" to FontAwesomeIcons.Brands.CcApplePay,
            "CcDinersClub" to FontAwesomeIcons.Brands.CcDinersClub,
            "CcDiscover" to FontAwesomeIcons.Brands.CcDiscover,
            "CcJcb" to FontAwesomeIcons.Brands.CcJcb,
            "CcMastercard" to FontAwesomeIcons.Brands.CcMastercard,
            "CcPaypal" to FontAwesomeIcons.Brands.CcPaypal,
            "CcStripe" to FontAwesomeIcons.Brands.CcStripe,
            "CcVisa" to FontAwesomeIcons.Brands.CcVisa,
            "Centercode" to FontAwesomeIcons.Brands.Centercode,
            "Centos" to FontAwesomeIcons.Brands.Centos,
            "Chrome" to FontAwesomeIcons.Brands.Chrome,
            "Chromecast" to FontAwesomeIcons.Brands.Chromecast,
            "Cloudflare" to FontAwesomeIcons.Brands.Cloudflare,
            "Cloudscale" to FontAwesomeIcons.Brands.Cloudscale,
            "Cloudsmith" to FontAwesomeIcons.Brands.Cloudsmith,
            "Cloudversify" to FontAwesomeIcons.Brands.Cloudversify,
            "Codepen" to FontAwesomeIcons.Brands.Codepen,
            "Codiepie" to FontAwesomeIcons.Brands.Codiepie,
            "Confluence" to FontAwesomeIcons.Brands.Confluence,
            "Connectdevelop" to FontAwesomeIcons.Brands.Connectdevelop,
            "Contao" to FontAwesomeIcons.Brands.Contao,
            "CottonBureau" to FontAwesomeIcons.Brands.CottonBureau,
            "Cpanel" to FontAwesomeIcons.Brands.Cpanel,
            "CreativeCommons" to FontAwesomeIcons.Brands.CreativeCommons,
            "CreativeCommonsBy" to FontAwesomeIcons.Brands.CreativeCommonsBy,
            "CreativeCommonsNc" to FontAwesomeIcons.Brands.CreativeCommonsNc,
            "CreativeCommonsNcEu" to FontAwesomeIcons.Brands.CreativeCommonsNcEu,
            "CreativeCommonsNcJp" to FontAwesomeIcons.Brands.CreativeCommonsNcJp,
            "CreativeCommonsNd" to FontAwesomeIcons.Brands.CreativeCommonsNd,
            "CreativeCommonsPd" to FontAwesomeIcons.Brands.CreativeCommonsPd,
            "CreativeCommonsPdAlt" to FontAwesomeIcons.Brands.CreativeCommonsPdAlt,
            "CreativeCommonsRemix" to FontAwesomeIcons.Brands.CreativeCommonsRemix,
            "CreativeCommonsSa" to FontAwesomeIcons.Brands.CreativeCommonsSa,
            "CreativeCommonsSampling" to FontAwesomeIcons.Brands.CreativeCommonsSampling,
            "CreativeCommonsSamplingPlus" to FontAwesomeIcons.Brands.CreativeCommonsSamplingPlus,
            "CreativeCommonsShare" to FontAwesomeIcons.Brands.CreativeCommonsShare,
            "CreativeCommonsZero" to FontAwesomeIcons.Brands.CreativeCommonsZero,
            "CriticalRole" to FontAwesomeIcons.Brands.CriticalRole,
            "Css3" to FontAwesomeIcons.Brands.Css3,
            "Css3Alt" to FontAwesomeIcons.Brands.Css3Alt,
            "Cuttlefish" to FontAwesomeIcons.Brands.Cuttlefish,
            "DAndD" to FontAwesomeIcons.Brands.DAndD,
            "DAndDBeyond" to FontAwesomeIcons.Brands.DAndDBeyond,
            "Dailymotion" to FontAwesomeIcons.Brands.Dailymotion,
            "Dashcube" to FontAwesomeIcons.Brands.Dashcube,
            "Deezer" to FontAwesomeIcons.Brands.Deezer,
            "Delicious" to FontAwesomeIcons.Brands.Delicious,
            "Deploydog" to FontAwesomeIcons.Brands.Deploydog,
            "Deskpro" to FontAwesomeIcons.Brands.Deskpro,
            "Dev" to FontAwesomeIcons.Brands.Dev,
            "Deviantart" to FontAwesomeIcons.Brands.Deviantart,
            "Dhl" to FontAwesomeIcons.Brands.Dhl,
            "Diaspora" to FontAwesomeIcons.Brands.Diaspora,
            "Digg" to FontAwesomeIcons.Brands.Digg,
            "DigitalOcean" to FontAwesomeIcons.Brands.DigitalOcean,
            "Discord" to FontAwesomeIcons.Brands.Discord,
            "Discourse" to FontAwesomeIcons.Brands.Discourse,
            "Dochub" to FontAwesomeIcons.Brands.Dochub,
            "Docker" to FontAwesomeIcons.Brands.Docker,
            "Draft2digital" to FontAwesomeIcons.Brands.Draft2digital,
            "Dribbble" to FontAwesomeIcons.Brands.Dribbble,
            "DribbbleSquare" to FontAwesomeIcons.Brands.DribbbleSquare,
            "Dropbox" to FontAwesomeIcons.Brands.Dropbox,
            "Drupal" to FontAwesomeIcons.Brands.Drupal,
            "Dyalog" to FontAwesomeIcons.Brands.Dyalog,
            "Earlybirds" to FontAwesomeIcons.Brands.Earlybirds,
            "Ebay" to FontAwesomeIcons.Brands.Ebay,
            "Edge" to FontAwesomeIcons.Brands.Edge,
            "EdgeLegacy" to FontAwesomeIcons.Brands.EdgeLegacy,
            "Elementor" to FontAwesomeIcons.Brands.Elementor,
            "Ello" to FontAwesomeIcons.Brands.Ello,
            "Ember" to FontAwesomeIcons.Brands.Ember,
            "Empire" to FontAwesomeIcons.Brands.Empire,
            "Envira" to FontAwesomeIcons.Brands.Envira,
            "Erlang" to FontAwesomeIcons.Brands.Erlang,
            "Ethereum" to FontAwesomeIcons.Brands.Ethereum,
            "Etsy" to FontAwesomeIcons.Brands.Etsy,
            "Evernote" to FontAwesomeIcons.Brands.Evernote,
            "Expeditedssl" to FontAwesomeIcons.Brands.Expeditedssl,
            "Facebook" to FontAwesomeIcons.Brands.Facebook,
            "FacebookF" to FontAwesomeIcons.Brands.FacebookF,
            "FacebookMessenger" to FontAwesomeIcons.Brands.FacebookMessenger,
            "FacebookSquare" to FontAwesomeIcons.Brands.FacebookSquare,
            "FantasyFlightGames" to FontAwesomeIcons.Brands.FantasyFlightGames,
            "Fedex" to FontAwesomeIcons.Brands.Fedex,
            "Fedora" to FontAwesomeIcons.Brands.Fedora,
            "Figma" to FontAwesomeIcons.Brands.Figma,
            "Firefox" to FontAwesomeIcons.Brands.Firefox,
            "FirefoxBrowser" to FontAwesomeIcons.Brands.FirefoxBrowser,
            "FirstOrder" to FontAwesomeIcons.Brands.FirstOrder,
            "FirstOrderAlt" to FontAwesomeIcons.Brands.FirstOrderAlt,
            "Firstdraft" to FontAwesomeIcons.Brands.Firstdraft,
            "Flickr" to FontAwesomeIcons.Brands.Flickr,
            "Flipboard" to FontAwesomeIcons.Brands.Flipboard,
            "Fly" to FontAwesomeIcons.Brands.Fly,
            "FontAwesome" to FontAwesomeIcons.Brands.FontAwesome,
            "FontAwesomeAlt" to FontAwesomeIcons.Brands.FontAwesomeAlt,
            "FontAwesomeFlag" to FontAwesomeIcons.Brands.FontAwesomeFlag,
            "FontAwesomeLogoFull" to FontAwesomeIcons.Brands.FontAwesomeLogoFull,
            "Fonticons" to FontAwesomeIcons.Brands.Fonticons,
            "FonticonsFi" to FontAwesomeIcons.Brands.FonticonsFi,
            "FortAwesome" to FontAwesomeIcons.Brands.FortAwesome,
            "FortAwesomeAlt" to FontAwesomeIcons.Brands.FortAwesomeAlt,
            "Forumbee" to FontAwesomeIcons.Brands.Forumbee,
            "Foursquare" to FontAwesomeIcons.Brands.Foursquare,
            "FreeCodeCamp" to FontAwesomeIcons.Brands.FreeCodeCamp,
            "Freebsd" to FontAwesomeIcons.Brands.Freebsd,
            "Fulcrum" to FontAwesomeIcons.Brands.Fulcrum,
            "GalacticRepublic" to FontAwesomeIcons.Brands.GalacticRepublic,
            "GalacticSenate" to FontAwesomeIcons.Brands.GalacticSenate,
            "GetPocket" to FontAwesomeIcons.Brands.GetPocket,
            "Gg" to FontAwesomeIcons.Brands.Gg,
            "GgCircle" to FontAwesomeIcons.Brands.GgCircle,
            "Git" to FontAwesomeIcons.Brands.Git,
            "GitAlt" to FontAwesomeIcons.Brands.GitAlt,
            "GitSquare" to FontAwesomeIcons.Brands.GitSquare,
            "Github" to FontAwesomeIcons.Brands.Github,
            "GithubAlt" to FontAwesomeIcons.Brands.GithubAlt,
            "GithubSquare" to FontAwesomeIcons.Brands.GithubSquare,
            "Gitkraken" to FontAwesomeIcons.Brands.Gitkraken,
            "Gitlab" to FontAwesomeIcons.Brands.Gitlab,
            "Gitter" to FontAwesomeIcons.Brands.Gitter,
            "Glide" to FontAwesomeIcons.Brands.Glide,
            "GlideG" to FontAwesomeIcons.Brands.GlideG,
            "Gofore" to FontAwesomeIcons.Brands.Gofore,
            "Goodreads" to FontAwesomeIcons.Brands.Goodreads,
            "GoodreadsG" to FontAwesomeIcons.Brands.GoodreadsG,
            "Google" to FontAwesomeIcons.Brands.Google,
            "GoogleDrive" to FontAwesomeIcons.Brands.GoogleDrive,
            "GooglePay" to FontAwesomeIcons.Brands.GooglePay,
            "GooglePlay" to FontAwesomeIcons.Brands.GooglePlay,
            "GooglePlus" to FontAwesomeIcons.Brands.GooglePlus,
            "GooglePlusG" to FontAwesomeIcons.Brands.GooglePlusG,
            "GooglePlusSquare" to FontAwesomeIcons.Brands.GooglePlusSquare,
            "GoogleWallet" to FontAwesomeIcons.Brands.GoogleWallet,
            "Gratipay" to FontAwesomeIcons.Brands.Gratipay,
            "Grav" to FontAwesomeIcons.Brands.Grav,
            "Gripfire" to FontAwesomeIcons.Brands.Gripfire,
            "Grunt" to FontAwesomeIcons.Brands.Grunt,
            "Guilded" to FontAwesomeIcons.Brands.Guilded,
            "Gulp" to FontAwesomeIcons.Brands.Gulp,
            "HackerNews" to FontAwesomeIcons.Brands.HackerNews,
            "HackerNewsSquare" to FontAwesomeIcons.Brands.HackerNewsSquare,
            "Hackerrank" to FontAwesomeIcons.Brands.Hackerrank,
            "Hips" to FontAwesomeIcons.Brands.Hips,
            "HireAHelper" to FontAwesomeIcons.Brands.HireAHelper,
            "Hive" to FontAwesomeIcons.Brands.Hive,
            "Hooli" to FontAwesomeIcons.Brands.Hooli,
            "Hornbill" to FontAwesomeIcons.Brands.Hornbill,
            "Hotjar" to FontAwesomeIcons.Brands.Hotjar,
            "Houzz" to FontAwesomeIcons.Brands.Houzz,
            "Html5" to FontAwesomeIcons.Brands.Html5,
            "Hubspot" to FontAwesomeIcons.Brands.Hubspot,
            "Ideal" to FontAwesomeIcons.Brands.Ideal,
            "Imdb" to FontAwesomeIcons.Brands.Imdb,
            "Innosoft" to FontAwesomeIcons.Brands.Innosoft,
            "Instagram" to FontAwesomeIcons.Brands.Instagram,
            "InstagramSquare" to FontAwesomeIcons.Brands.InstagramSquare,
            "Instalod" to FontAwesomeIcons.Brands.Instalod,
            "Intercom" to FontAwesomeIcons.Brands.Intercom,
            "InternetExplorer" to FontAwesomeIcons.Brands.InternetExplorer,
            "Invision" to FontAwesomeIcons.Brands.Invision,
            "Ioxhost" to FontAwesomeIcons.Brands.Ioxhost,
            "ItchIo" to FontAwesomeIcons.Brands.ItchIo,
            "Itunes" to FontAwesomeIcons.Brands.Itunes,
            "ItunesNote" to FontAwesomeIcons.Brands.ItunesNote,
            "Java" to FontAwesomeIcons.Brands.Java,
            "JediOrder" to FontAwesomeIcons.Brands.JediOrder,
            "Jenkins" to FontAwesomeIcons.Brands.Jenkins,
            "Jira" to FontAwesomeIcons.Brands.Jira,
            "Joget" to FontAwesomeIcons.Brands.Joget,
            "Joomla" to FontAwesomeIcons.Brands.Joomla,
            "Js" to FontAwesomeIcons.Brands.Js,
            "JsSquare" to FontAwesomeIcons.Brands.JsSquare,
            "Jsfiddle" to FontAwesomeIcons.Brands.Jsfiddle,
            "Kaggle" to FontAwesomeIcons.Brands.Kaggle,
            "Keybase" to FontAwesomeIcons.Brands.Keybase,
            "Keycdn" to FontAwesomeIcons.Brands.Keycdn,
            "Kickstarter" to FontAwesomeIcons.Brands.Kickstarter,
            "KickstarterK" to FontAwesomeIcons.Brands.KickstarterK,
            "Korvue" to FontAwesomeIcons.Brands.Korvue,
            "Laravel" to FontAwesomeIcons.Brands.Laravel,
            "Lastfm" to FontAwesomeIcons.Brands.Lastfm,
            "LastfmSquare" to FontAwesomeIcons.Brands.LastfmSquare,
            "Leanpub" to FontAwesomeIcons.Brands.Leanpub,
            "Less" to FontAwesomeIcons.Brands.Less,
            "Line" to FontAwesomeIcons.Brands.Line,
            "Linkedin" to FontAwesomeIcons.Brands.Linkedin,
            "LinkedinIn" to FontAwesomeIcons.Brands.LinkedinIn,
            "Linode" to FontAwesomeIcons.Brands.Linode,
            "Linux" to FontAwesomeIcons.Brands.Linux,
            "Lyft" to FontAwesomeIcons.Brands.Lyft,
            "Magento" to FontAwesomeIcons.Brands.Magento,
            "Mailchimp" to FontAwesomeIcons.Brands.Mailchimp,
            "Mandalorian" to FontAwesomeIcons.Brands.Mandalorian,
            "Markdown" to FontAwesomeIcons.Brands.Markdown,
            "Mastodon" to FontAwesomeIcons.Brands.Mastodon,
            "Maxcdn" to FontAwesomeIcons.Brands.Maxcdn,
            "Mdb" to FontAwesomeIcons.Brands.Mdb,
            "Medapps" to FontAwesomeIcons.Brands.Medapps,
            "Medium" to FontAwesomeIcons.Brands.Medium,
            "MediumM" to FontAwesomeIcons.Brands.MediumM,
            "Medrt" to FontAwesomeIcons.Brands.Medrt,
            "Meetup" to FontAwesomeIcons.Brands.Meetup,
            "Megaport" to FontAwesomeIcons.Brands.Megaport,
            "Mendeley" to FontAwesomeIcons.Brands.Mendeley,
            "Microblog" to FontAwesomeIcons.Brands.Microblog,
            "Microsoft" to FontAwesomeIcons.Brands.Microsoft,
            "Mix" to FontAwesomeIcons.Brands.Mix,
            "Mixcloud" to FontAwesomeIcons.Brands.Mixcloud,
            "Mixer" to FontAwesomeIcons.Brands.Mixer,
            "Mizuni" to FontAwesomeIcons.Brands.Mizuni,
            "Modx" to FontAwesomeIcons.Brands.Modx,
            "Monero" to FontAwesomeIcons.Brands.Monero,
            "Napster" to FontAwesomeIcons.Brands.Napster,
            "Neos" to FontAwesomeIcons.Brands.Neos,
            "Nimblr" to FontAwesomeIcons.Brands.Nimblr,
            "Node" to FontAwesomeIcons.Brands.Node,
            "NodeJs" to FontAwesomeIcons.Brands.NodeJs,
            "Npm" to FontAwesomeIcons.Brands.Npm,
            "Ns8" to FontAwesomeIcons.Brands.Ns8,
            "Nutritionix" to FontAwesomeIcons.Brands.Nutritionix,
            "OctopusDeploy" to FontAwesomeIcons.Brands.OctopusDeploy,
            "Odnoklassniki" to FontAwesomeIcons.Brands.Odnoklassniki,
            "OdnoklassnikiSquare" to FontAwesomeIcons.Brands.OdnoklassnikiSquare,
            "OldRepublic" to FontAwesomeIcons.Brands.OldRepublic,
            "Opencart" to FontAwesomeIcons.Brands.Opencart,
            "Openid" to FontAwesomeIcons.Brands.Openid,
            "Opera" to FontAwesomeIcons.Brands.Opera,
            "OptinMonster" to FontAwesomeIcons.Brands.OptinMonster,
            "Orcid" to FontAwesomeIcons.Brands.Orcid,
            "Osi" to FontAwesomeIcons.Brands.Osi,
            "Page4" to FontAwesomeIcons.Brands.Page4,
            "Pagelines" to FontAwesomeIcons.Brands.Pagelines,
            "Palfed" to FontAwesomeIcons.Brands.Palfed,
            "Patreon" to FontAwesomeIcons.Brands.Patreon,
            "Paypal" to FontAwesomeIcons.Brands.Paypal,
            "PennyArcade" to FontAwesomeIcons.Brands.PennyArcade,
            "Perbyte" to FontAwesomeIcons.Brands.Perbyte,
            "Periscope" to FontAwesomeIcons.Brands.Periscope,
            "Phabricator" to FontAwesomeIcons.Brands.Phabricator,
            "PhoenixFramework" to FontAwesomeIcons.Brands.PhoenixFramework,
            "PhoenixSquadron" to FontAwesomeIcons.Brands.PhoenixSquadron,
            "Php" to FontAwesomeIcons.Brands.Php,
            "PiedPiper" to FontAwesomeIcons.Brands.PiedPiper,
            "PiedPiperAlt" to FontAwesomeIcons.Brands.PiedPiperAlt,
            "PiedPiperHat" to FontAwesomeIcons.Brands.PiedPiperHat,
            "PiedPiperPp" to FontAwesomeIcons.Brands.PiedPiperPp,
            "PiedPiperSquare" to FontAwesomeIcons.Brands.PiedPiperSquare,
            "Pinterest" to FontAwesomeIcons.Brands.Pinterest,
            "PinterestP" to FontAwesomeIcons.Brands.PinterestP,
            "PinterestSquare" to FontAwesomeIcons.Brands.PinterestSquare,
            "Playstation" to FontAwesomeIcons.Brands.Playstation,
            "ProductHunt" to FontAwesomeIcons.Brands.ProductHunt,
            "Pushed" to FontAwesomeIcons.Brands.Pushed,
            "Python" to FontAwesomeIcons.Brands.Python,
            "Qq" to FontAwesomeIcons.Brands.Qq,
            "Quinscape" to FontAwesomeIcons.Brands.Quinscape,
            "Quora" to FontAwesomeIcons.Brands.Quora,
            "RProject" to FontAwesomeIcons.Brands.RProject,
            "RaspberryPi" to FontAwesomeIcons.Brands.RaspberryPi,
            "Ravelry" to FontAwesomeIcons.Brands.Ravelry,
            "React" to FontAwesomeIcons.Brands.React,
            "Reacteurope" to FontAwesomeIcons.Brands.Reacteurope,
            "Readme" to FontAwesomeIcons.Brands.Readme,
            "Rebel" to FontAwesomeIcons.Brands.Rebel,
            "RedRiver" to FontAwesomeIcons.Brands.RedRiver,
            "Reddit" to FontAwesomeIcons.Brands.Reddit,
            "RedditAlien" to FontAwesomeIcons.Brands.RedditAlien,
            "RedditSquare" to FontAwesomeIcons.Brands.RedditSquare,
            "Redhat" to FontAwesomeIcons.Brands.Redhat,
            "Renren" to FontAwesomeIcons.Brands.Renren,
            "Replyd" to FontAwesomeIcons.Brands.Replyd,
            "Researchgate" to FontAwesomeIcons.Brands.Researchgate,
            "Resolving" to FontAwesomeIcons.Brands.Resolving,
            "Rev" to FontAwesomeIcons.Brands.Rev,
            "Rocketchat" to FontAwesomeIcons.Brands.Rocketchat,
            "Rockrms" to FontAwesomeIcons.Brands.Rockrms,
            "Rust" to FontAwesomeIcons.Brands.Rust,
            "Safari" to FontAwesomeIcons.Brands.Safari,
            "Salesforce" to FontAwesomeIcons.Brands.Salesforce,
            "Sass" to FontAwesomeIcons.Brands.Sass,
            "Schlix" to FontAwesomeIcons.Brands.Schlix,
            "Scribd" to FontAwesomeIcons.Brands.Scribd,
            "Searchengin" to FontAwesomeIcons.Brands.Searchengin,
            "Sellcast" to FontAwesomeIcons.Brands.Sellcast,
            "Sellsy" to FontAwesomeIcons.Brands.Sellsy,
            "Servicestack" to FontAwesomeIcons.Brands.Servicestack,
            "Shirtsinbulk" to FontAwesomeIcons.Brands.Shirtsinbulk,
            "Shopify" to FontAwesomeIcons.Brands.Shopify,
            "Shopware" to FontAwesomeIcons.Brands.Shopware,
            "Simplybuilt" to FontAwesomeIcons.Brands.Simplybuilt,
            "Sistrix" to FontAwesomeIcons.Brands.Sistrix,
            "Sith" to FontAwesomeIcons.Brands.Sith,
            "Sketch" to FontAwesomeIcons.Brands.Sketch,
            "Skyatlas" to FontAwesomeIcons.Brands.Skyatlas,
            "Skype" to FontAwesomeIcons.Brands.Skype,
            "Slack" to FontAwesomeIcons.Brands.Slack,
            "SlackHash" to FontAwesomeIcons.Brands.SlackHash,
            "Slideshare" to FontAwesomeIcons.Brands.Slideshare,
            "Snapchat" to FontAwesomeIcons.Brands.Snapchat,
            "SnapchatGhost" to FontAwesomeIcons.Brands.SnapchatGhost,
            "SnapchatSquare" to FontAwesomeIcons.Brands.SnapchatSquare,
            "Soundcloud" to FontAwesomeIcons.Brands.Soundcloud,
            "Sourcetree" to FontAwesomeIcons.Brands.Sourcetree,
            "Speakap" to FontAwesomeIcons.Brands.Speakap,
            "SpeakerDeck" to FontAwesomeIcons.Brands.SpeakerDeck,
            "Spotify" to FontAwesomeIcons.Brands.Spotify,
            "Squarespace" to FontAwesomeIcons.Brands.Squarespace,
            "StackExchange" to FontAwesomeIcons.Brands.StackExchange,
            "StackOverflow" to FontAwesomeIcons.Brands.StackOverflow,
            "Stackpath" to FontAwesomeIcons.Brands.Stackpath,
            "Staylinked" to FontAwesomeIcons.Brands.Staylinked,
            "Steam" to FontAwesomeIcons.Brands.Steam,
            "SteamSquare" to FontAwesomeIcons.Brands.SteamSquare,
            "SteamSymbol" to FontAwesomeIcons.Brands.SteamSymbol,
            "StickerMule" to FontAwesomeIcons.Brands.StickerMule,
            "Strava" to FontAwesomeIcons.Brands.Strava,
            "Stripe" to FontAwesomeIcons.Brands.Stripe,
            "StripeS" to FontAwesomeIcons.Brands.StripeS,
            "Studiovinari" to FontAwesomeIcons.Brands.Studiovinari,
            "Stumbleupon" to FontAwesomeIcons.Brands.Stumbleupon,
            "StumbleuponCircle" to FontAwesomeIcons.Brands.StumbleuponCircle,
            "Superpowers" to FontAwesomeIcons.Brands.Superpowers,
            "Supple" to FontAwesomeIcons.Brands.Supple,
            "Suse" to FontAwesomeIcons.Brands.Suse,
            "Swift" to FontAwesomeIcons.Brands.Swift,
            "Symfony" to FontAwesomeIcons.Brands.Symfony,
            "Teamspeak" to FontAwesomeIcons.Brands.Teamspeak,
            "Telegram" to FontAwesomeIcons.Brands.Telegram,
            "TelegramPlane" to FontAwesomeIcons.Brands.TelegramPlane,
            "TencentWeibo" to FontAwesomeIcons.Brands.TencentWeibo,
            "TheRedYeti" to FontAwesomeIcons.Brands.TheRedYeti,
            "Themeco" to FontAwesomeIcons.Brands.Themeco,
            "Themeisle" to FontAwesomeIcons.Brands.Themeisle,
            "ThinkPeaks" to FontAwesomeIcons.Brands.ThinkPeaks,
            "Tiktok" to FontAwesomeIcons.Brands.Tiktok,
            "TradeFederation" to FontAwesomeIcons.Brands.TradeFederation,
            "Trello" to FontAwesomeIcons.Brands.Trello,
            "Tripadvisor" to FontAwesomeIcons.Brands.Tripadvisor,
            "Tumblr" to FontAwesomeIcons.Brands.Tumblr,
            "TumblrSquare" to FontAwesomeIcons.Brands.TumblrSquare,
            "Twitch" to FontAwesomeIcons.Brands.Twitch,
            "Twitter" to FontAwesomeIcons.Brands.Twitter,
            "TwitterSquare" to FontAwesomeIcons.Brands.TwitterSquare,
            "Typo3" to FontAwesomeIcons.Brands.Typo3,
            "Uber" to FontAwesomeIcons.Brands.Uber,
            "Ubuntu" to FontAwesomeIcons.Brands.Ubuntu,
            "Uikit" to FontAwesomeIcons.Brands.Uikit,
            "Umbraco" to FontAwesomeIcons.Brands.Umbraco,
            "Uncharted" to FontAwesomeIcons.Brands.Uncharted,
            "Uniregistry" to FontAwesomeIcons.Brands.Uniregistry,
            "Unity" to FontAwesomeIcons.Brands.Unity,
            "Unsplash" to FontAwesomeIcons.Brands.Unsplash,
            "Untappd" to FontAwesomeIcons.Brands.Untappd,
            "Ups" to FontAwesomeIcons.Brands.Ups,
            "Usb" to FontAwesomeIcons.Brands.Usb,
            "Usps" to FontAwesomeIcons.Brands.Usps,
            "Ussunnah" to FontAwesomeIcons.Brands.Ussunnah,
            "Vaadin" to FontAwesomeIcons.Brands.Vaadin,
            "Viacoin" to FontAwesomeIcons.Brands.Viacoin,
            "Viadeo" to FontAwesomeIcons.Brands.Viadeo,
            "ViadeoSquare" to FontAwesomeIcons.Brands.ViadeoSquare,
            "Viber" to FontAwesomeIcons.Brands.Viber,
            "Vimeo" to FontAwesomeIcons.Brands.Vimeo,
            "VimeoSquare" to FontAwesomeIcons.Brands.VimeoSquare,
            "VimeoV" to FontAwesomeIcons.Brands.VimeoV,
            "Vine" to FontAwesomeIcons.Brands.Vine,
            "Vk" to FontAwesomeIcons.Brands.Vk,
            "Vnv" to FontAwesomeIcons.Brands.Vnv,
            "Vuejs" to FontAwesomeIcons.Brands.Vuejs,
            "WatchmanMonitoring" to FontAwesomeIcons.Brands.WatchmanMonitoring,
            "Waze" to FontAwesomeIcons.Brands.Waze,
            "Weebly" to FontAwesomeIcons.Brands.Weebly,
            "Weibo" to FontAwesomeIcons.Brands.Weibo,
            "Weixin" to FontAwesomeIcons.Brands.Weixin,
            "Whatsapp" to FontAwesomeIcons.Brands.Whatsapp,
            "WhatsappSquare" to FontAwesomeIcons.Brands.WhatsappSquare,
            "Whmcs" to FontAwesomeIcons.Brands.Whmcs,
            "WikipediaW" to FontAwesomeIcons.Brands.WikipediaW,
            "Windows" to FontAwesomeIcons.Brands.Windows,
            "Wix" to FontAwesomeIcons.Brands.Wix,
            "WizardsOfTheCoast" to FontAwesomeIcons.Brands.WizardsOfTheCoast,
            "Wodu" to FontAwesomeIcons.Brands.Wodu,
            "WolfPackBattalion" to FontAwesomeIcons.Brands.WolfPackBattalion,
            "Wordpress" to FontAwesomeIcons.Brands.Wordpress,
            "WordpressSimple" to FontAwesomeIcons.Brands.WordpressSimple,
            "Wpbeginner" to FontAwesomeIcons.Brands.Wpbeginner,
            "Wpexplorer" to FontAwesomeIcons.Brands.Wpexplorer,
            "Wpforms" to FontAwesomeIcons.Brands.Wpforms,
            "Wpressr" to FontAwesomeIcons.Brands.Wpressr,
            "Xbox" to FontAwesomeIcons.Brands.Xbox,
            "Xing" to FontAwesomeIcons.Brands.Xing,
            "XingSquare" to FontAwesomeIcons.Brands.XingSquare,
            "YCombinator" to FontAwesomeIcons.Brands.YCombinator,
            "Yahoo" to FontAwesomeIcons.Brands.Yahoo,
            "Yammer" to FontAwesomeIcons.Brands.Yammer,
            "Yandex" to FontAwesomeIcons.Brands.Yandex,
            "YandexInternational" to FontAwesomeIcons.Brands.YandexInternational,
            "Yarn" to FontAwesomeIcons.Brands.Yarn,
            "Yelp" to FontAwesomeIcons.Brands.Yelp,
            "Yoast" to FontAwesomeIcons.Brands.Yoast,
            "Youtube" to FontAwesomeIcons.Brands.Youtube,
            "YoutubeSquare" to FontAwesomeIcons.Brands.YoutubeSquare,
            "Zhihu" to FontAwesomeIcons.Brands.Zhihu,
        )
    }

    val featherIcon: Map<String, ImageVector> by lazy {
        mapOf(
            "Activity" to FeatherIcons.Activity,
            "Airplay" to FeatherIcons.Airplay,
            "AlertCircle" to FeatherIcons.AlertCircle,
            "AlertOctagon" to FeatherIcons.AlertOctagon,
            "AlertTriangle" to FeatherIcons.AlertTriangle,
            "AlignCenter" to FeatherIcons.AlignCenter,
            "AlignJustify" to FeatherIcons.AlignJustify,
            "AlignLeft" to FeatherIcons.AlignLeft,
            "AlignRight" to FeatherIcons.AlignRight,
            "Anchor" to FeatherIcons.Anchor,
            "Aperture" to FeatherIcons.Aperture,
            "Archive" to FeatherIcons.Archive,
            "ArrowDown" to FeatherIcons.ArrowDown,
            "ArrowDownCircle" to FeatherIcons.ArrowDownCircle,
            "ArrowDownLeft" to FeatherIcons.ArrowDownLeft,
            "ArrowDownRight" to FeatherIcons.ArrowDownRight,
            "ArrowLeft" to FeatherIcons.ArrowLeft,
            "ArrowLeftCircle" to FeatherIcons.ArrowLeftCircle,
            "ArrowRight" to FeatherIcons.ArrowRight,
            "ArrowRightCircle" to FeatherIcons.ArrowRightCircle,
            "ArrowUp" to FeatherIcons.ArrowUp,
            "ArrowUpCircle" to FeatherIcons.ArrowUpCircle,
            "ArrowUpLeft" to FeatherIcons.ArrowUpLeft,
            "ArrowUpRight" to FeatherIcons.ArrowUpRight,
            "AtSign" to FeatherIcons.AtSign,
            "Award" to FeatherIcons.Award,
            "BarChart" to FeatherIcons.BarChart,
            "BarChart2" to FeatherIcons.BarChart2,
            "Battery" to FeatherIcons.Battery,
            "BatteryCharging" to FeatherIcons.BatteryCharging,
            "Bell" to FeatherIcons.Bell,
            "BellOff" to FeatherIcons.BellOff,
            "Bluetooth" to FeatherIcons.Bluetooth,
            "Bold" to FeatherIcons.Bold,
            "Book" to FeatherIcons.Book,
            "BookOpen" to FeatherIcons.BookOpen,
            "Bookmark" to FeatherIcons.Bookmark,
            "Box" to FeatherIcons.Box,
            "Briefcase" to FeatherIcons.Briefcase,
            "Calendar" to FeatherIcons.Calendar,
            "Camera" to FeatherIcons.Camera,
            "CameraOff" to FeatherIcons.CameraOff,
            "Cast" to FeatherIcons.Cast,
            "Check" to FeatherIcons.Check,
            "CheckCircle" to FeatherIcons.CheckCircle,
            "CheckSquare" to FeatherIcons.CheckSquare,
            "ChevronDown" to FeatherIcons.ChevronDown,
            "ChevronLeft" to FeatherIcons.ChevronLeft,
            "ChevronRight" to FeatherIcons.ChevronRight,
            "ChevronUp" to FeatherIcons.ChevronUp,
            "ChevronsDown" to FeatherIcons.ChevronsDown,
            "ChevronsLeft" to FeatherIcons.ChevronsLeft,
            "ChevronsRight" to FeatherIcons.ChevronsRight,
            "ChevronsUp" to FeatherIcons.ChevronsUp,
            "Chrome" to FeatherIcons.Chrome,
            "Circle" to FeatherIcons.Circle,
            "Clipboard" to FeatherIcons.Clipboard,
            "Clock" to FeatherIcons.Clock,
            "Cloud" to FeatherIcons.Cloud,
            "CloudDrizzle" to FeatherIcons.CloudDrizzle,
            "CloudLightning" to FeatherIcons.CloudLightning,
            "CloudOff" to FeatherIcons.CloudOff,
            "CloudRain" to FeatherIcons.CloudRain,
            "CloudSnow" to FeatherIcons.CloudSnow,
            "Code" to FeatherIcons.Code,
            "Codepen" to FeatherIcons.Codepen,
            "Codesandbox" to FeatherIcons.Codesandbox,
            "Coffee" to FeatherIcons.Coffee,
            "Columns" to FeatherIcons.Columns,
            "Command" to FeatherIcons.Command,
            "Compass" to FeatherIcons.Compass,
            "Copy" to FeatherIcons.Copy,
            "CornerDownLeft" to FeatherIcons.CornerDownLeft,
            "CornerDownRight" to FeatherIcons.CornerDownRight,
            "CornerLeftDown" to FeatherIcons.CornerLeftDown,
            "CornerLeftUp" to FeatherIcons.CornerLeftUp,
            "CornerRightDown" to FeatherIcons.CornerRightDown,
            "CornerRightUp" to FeatherIcons.CornerRightUp,
            "CornerUpLeft" to FeatherIcons.CornerUpLeft,
            "CornerUpRight" to FeatherIcons.CornerUpRight,
            "Cpu" to FeatherIcons.Cpu,
            "CreditCard" to FeatherIcons.CreditCard,
            "Crop" to FeatherIcons.Crop,
            "Crosshair" to FeatherIcons.Crosshair,
            "Database" to FeatherIcons.Database,
            "Delete" to FeatherIcons.Delete,
            "Disc" to FeatherIcons.Disc,
            "Divide" to FeatherIcons.Divide,
            "DivideCircle" to FeatherIcons.DivideCircle,
            "DivideSquare" to FeatherIcons.DivideSquare,
            "DollarSign" to FeatherIcons.DollarSign,
            "Download" to FeatherIcons.Download,
            "DownloadCloud" to FeatherIcons.DownloadCloud,
            "Dribbble" to FeatherIcons.Dribbble,
            "Droplet" to FeatherIcons.Droplet,
            "Edit" to FeatherIcons.Edit,
            "Edit2" to FeatherIcons.Edit2,
            "Edit3" to FeatherIcons.Edit3,
            "ExternalLink" to FeatherIcons.ExternalLink,
            "Eye" to FeatherIcons.Eye,
            "EyeOff" to FeatherIcons.EyeOff,
            "Facebook" to FeatherIcons.Facebook,
            "FastForward" to FeatherIcons.FastForward,
            "Feather" to FeatherIcons.Feather,
            "Figma" to FeatherIcons.Figma,
            "File" to FeatherIcons.File,
            "FileMinus" to FeatherIcons.FileMinus,
            "FilePlus" to FeatherIcons.FilePlus,
            "FileText" to FeatherIcons.FileText,
            "Film" to FeatherIcons.Film,
            "Filter" to FeatherIcons.Filter,
            "Flag" to FeatherIcons.Flag,
            "Folder" to FeatherIcons.Folder,
            "FolderMinus" to FeatherIcons.FolderMinus,
            "FolderPlus" to FeatherIcons.FolderPlus,
            "Framer" to FeatherIcons.Framer,
            "Frown" to FeatherIcons.Frown,
            "Gift" to FeatherIcons.Gift,
            "GitBranch" to FeatherIcons.GitBranch,
            "GitCommit" to FeatherIcons.GitCommit,
            "GitMerge" to FeatherIcons.GitMerge,
            "GitPullRequest" to FeatherIcons.GitPullRequest,
            "Github" to FeatherIcons.Github,
            "Gitlab" to FeatherIcons.Gitlab,
            "Globe" to FeatherIcons.Globe,
            "Grid" to FeatherIcons.Grid,
            "HardDrive" to FeatherIcons.HardDrive,
            "Hash" to FeatherIcons.Hash,
            "Headphones" to FeatherIcons.Headphones,
            "Heart" to FeatherIcons.Heart,
            "HelpCircle" to FeatherIcons.HelpCircle,
            "Hexagon" to FeatherIcons.Hexagon,
            "Home" to FeatherIcons.Home,
            "Image" to FeatherIcons.Image,
            "Inbox" to FeatherIcons.Inbox,
            "Info" to FeatherIcons.Info,
            "Instagram" to FeatherIcons.Instagram,
            "Italic" to FeatherIcons.Italic,
            "Key" to FeatherIcons.Key,
            "Layers" to FeatherIcons.Layers,
            "Layout" to FeatherIcons.Layout,
            "LifeBuoy" to FeatherIcons.LifeBuoy,
            "Link" to FeatherIcons.Link,
            "Link2" to FeatherIcons.Link2,
            "Linkedin" to FeatherIcons.Linkedin,
            "List" to FeatherIcons.List,
            "Loader" to FeatherIcons.Loader,
            "Lock" to FeatherIcons.Lock,
            "LogIn" to FeatherIcons.LogIn,
            "LogOut" to FeatherIcons.LogOut,
            "Mail" to FeatherIcons.Mail,
            "Map" to FeatherIcons.Map,
            "MapPin" to FeatherIcons.MapPin,
            "Maximize" to FeatherIcons.Maximize,
            "Maximize2" to FeatherIcons.Maximize2,
            "Meh" to FeatherIcons.Meh,
            "Menu" to FeatherIcons.Menu,
            "MessageCircle" to FeatherIcons.MessageCircle,
            "MessageSquare" to FeatherIcons.MessageSquare,
            "Mic" to FeatherIcons.Mic,
            "MicOff" to FeatherIcons.MicOff,
            "Minimize" to FeatherIcons.Minimize,
            "Minimize2" to FeatherIcons.Minimize2,
            "Minus" to FeatherIcons.Minus,
            "MinusCircle" to FeatherIcons.MinusCircle,
            "MinusSquare" to FeatherIcons.MinusSquare,
            "Monitor" to FeatherIcons.Monitor,
            "Moon" to FeatherIcons.Moon,
            "MoreHorizontal" to FeatherIcons.MoreHorizontal,
            "MoreVertical" to FeatherIcons.MoreVertical,
            "MousePointer" to FeatherIcons.MousePointer,
            "Move" to FeatherIcons.Move,
            "Music" to FeatherIcons.Music,
            "Navigation" to FeatherIcons.Navigation,
            "Navigation2" to FeatherIcons.Navigation2,
            "Octagon" to FeatherIcons.Octagon,
            "Package" to FeatherIcons.Package,
            "Paperclip" to FeatherIcons.Paperclip,
            "Pause" to FeatherIcons.Pause,
            "PauseCircle" to FeatherIcons.PauseCircle,
            "PenTool" to FeatherIcons.PenTool,
            "Percent" to FeatherIcons.Percent,
            "Phone" to FeatherIcons.Phone,
            "PhoneCall" to FeatherIcons.PhoneCall,
            "PhoneForwarded" to FeatherIcons.PhoneForwarded,
            "PhoneIncoming" to FeatherIcons.PhoneIncoming,
            "PhoneMissed" to FeatherIcons.PhoneMissed,
            "PhoneOff" to FeatherIcons.PhoneOff,
            "PhoneOutgoing" to FeatherIcons.PhoneOutgoing,
            "PieChart" to FeatherIcons.PieChart,
            "Play" to FeatherIcons.Play,
            "PlayCircle" to FeatherIcons.PlayCircle,
            "Plus" to FeatherIcons.Plus,
            "PlusCircle" to FeatherIcons.PlusCircle,
            "PlusSquare" to FeatherIcons.PlusSquare,
            "Pocket" to FeatherIcons.Pocket,
            "Power" to FeatherIcons.Power,
            "Printer" to FeatherIcons.Printer,
            "Radio" to FeatherIcons.Radio,
            "RefreshCcw" to FeatherIcons.RefreshCcw,
            "RefreshCw" to FeatherIcons.RefreshCw,
            "Repeat" to FeatherIcons.Repeat,
            "Rewind" to FeatherIcons.Rewind,
            "RotateCcw" to FeatherIcons.RotateCcw,
            "RotateCw" to FeatherIcons.RotateCw,
            "Rss" to FeatherIcons.Rss,
            "Save" to FeatherIcons.Save,
            "Scissors" to FeatherIcons.Scissors,
            "Search" to FeatherIcons.Search,
            "Send" to FeatherIcons.Send,
            "Server" to FeatherIcons.Server,
            "Settings" to FeatherIcons.Settings,
            "Share" to FeatherIcons.Share,
            "Share2" to FeatherIcons.Share2,
            "Shield" to FeatherIcons.Shield,
            "ShieldOff" to FeatherIcons.ShieldOff,
            "ShoppingBag" to FeatherIcons.ShoppingBag,
            "ShoppingCart" to FeatherIcons.ShoppingCart,
            "Shuffle" to FeatherIcons.Shuffle,
            "Sidebar" to FeatherIcons.Sidebar,
            "SkipBack" to FeatherIcons.SkipBack,
            "SkipForward" to FeatherIcons.SkipForward,
            "Slack" to FeatherIcons.Slack,
            "Slash" to FeatherIcons.Slash,
            "Sliders" to FeatherIcons.Sliders,
            "Smartphone" to FeatherIcons.Smartphone,
            "Smile" to FeatherIcons.Smile,
            "Speaker" to FeatherIcons.Speaker,
            "Square" to FeatherIcons.Square,
            "Star" to FeatherIcons.Star,
            "StopCircle" to FeatherIcons.StopCircle,
            "Sun" to FeatherIcons.Sun,
            "Sunrise" to FeatherIcons.Sunrise,
            "Sunset" to FeatherIcons.Sunset,
            "Tablet" to FeatherIcons.Tablet,
            "Tag" to FeatherIcons.Tag,
            "Target" to FeatherIcons.Target,
            "Terminal" to FeatherIcons.Terminal,
            "Thermometer" to FeatherIcons.Thermometer,
            "ThumbsDown" to FeatherIcons.ThumbsDown,
            "ThumbsUp" to FeatherIcons.ThumbsUp,
            "ToggleLeft" to FeatherIcons.ToggleLeft,
            "ToggleRight" to FeatherIcons.ToggleRight,
            "Tool" to FeatherIcons.Tool,
            "Trash" to FeatherIcons.Trash,
            "Trash2" to FeatherIcons.Trash2,
            "Trello" to FeatherIcons.Trello,
            "TrendingDown" to FeatherIcons.TrendingDown,
            "TrendingUp" to FeatherIcons.TrendingUp,
            "Triangle" to FeatherIcons.Triangle,
            "Truck" to FeatherIcons.Truck,
            "Tv" to FeatherIcons.Tv,
            "Twitch" to FeatherIcons.Twitch,
            "Twitter" to FeatherIcons.Twitter,
            "Type" to FeatherIcons.Type,
            "Umbrella" to FeatherIcons.Umbrella,
            "Underline" to FeatherIcons.Underline,
            "Unlock" to FeatherIcons.Unlock,
            "Upload" to FeatherIcons.Upload,
            "UploadCloud" to FeatherIcons.UploadCloud,
            "User" to FeatherIcons.User,
            "UserCheck" to FeatherIcons.UserCheck,
            "UserMinus" to FeatherIcons.UserMinus,
            "UserPlus" to FeatherIcons.UserPlus,
            "UserX" to FeatherIcons.UserX,
            "Users" to FeatherIcons.Users,
            "Video" to FeatherIcons.Video,
            "VideoOff" to FeatherIcons.VideoOff,
            "Voicemail" to FeatherIcons.Voicemail,
            "Volume" to FeatherIcons.Volume,
            "Volume1" to FeatherIcons.Volume1,
            "Volume2" to FeatherIcons.Volume2,
            "VolumeX" to FeatherIcons.VolumeX,
            "Watch" to FeatherIcons.Watch,
            "Wifi" to FeatherIcons.Wifi,
            "WifiOff" to FeatherIcons.WifiOff,
            "Wind" to FeatherIcons.Wind,
            "X" to FeatherIcons.X,
            "XCircle" to FeatherIcons.XCircle,
            "XOctagon" to FeatherIcons.XOctagon,
            "XSquare" to FeatherIcons.XSquare,
            "Youtube" to FeatherIcons.Youtube,
            "Zap" to FeatherIcons.Zap,
            "ZapOff" to FeatherIcons.ZapOff,
            "ZoomIn" to FeatherIcons.ZoomIn,
            "ZoomOut" to FeatherIcons.ZoomOut
        )
    }

    val allIcons: Map<String, ImageVector> by lazy { featherIcon + regularIcons + solidIcons + brandIcon }
}